package com.quico.srhelper.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.quico.common.core.domain.entity.SysDictData;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.DictUtils;
import com.quico.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.service.helper.MaterialBindSyncService;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.cache.SrItemListCache;
import com.quico.srhelper.domain.dto.SrItemBindDTO;
import com.quico.srhelper.domain.vo.SrItemDetailVO;
import com.quico.srhelper.service.ISrItemService;
import com.quico.srhelper.config.SrhelperCacheConstants;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * 材料一览Service业务层处理
 * 
 * @author quico
 * @date 2026-05-25
 */
@Service
@Slf4j
public class SrItemServiceImpl implements ISrItemService 
{
    @Autowired
    private SrItemMapper srItemMapper;

    @Autowired
    private MaterialBindSyncService materialBindSyncService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /** 物品列表默认查询缓存 key */
    private static final String ITEM_LIST_CACHE_KEY = SrhelperCacheConstants.ITEM_LIST_KEY + "list";

    /**
     * 查询材料一览
     *
     * @param id 材料一览主键
     * @return 材料一览
     */
    @Override
    public SrItem selectSrItemById(Long id)
    {
        return srItemMapper.selectSrItemById(id);
    }

    /**
     * 查询材料详情（包含相关高阶素材）
     * 
     * @param id 材料一览主键
     * @return 材料详情VO
     */
    @Override
    public SrItemDetailVO selectSrItemDetailById(Long id)
    {
        SrItem item = srItemMapper.selectSrItemById(id);
        if (item == null) {
            return null;
        }
        
        SrItemDetailVO vo = new SrItemDetailVO();
        BeanUtils.copyProperties(item, vo);
        
        // 如果有 seriesId，查询同系列的其他材料
        if (item.getSeriesId() != null) {
            List<SrItem> seriesItems = srItemMapper.selectBySeriesId(item.getSeriesId());
            for (SrItem seriesItem : seriesItems) {
                // 排除当前材料本身
                if (seriesItem.getId().equals(id)) {
                    continue;
                }
                // 根据星级设置对应的材料
                if (seriesItem.getStarLevel() == 2) {
                    vo.setItem2Star(seriesItem);
                } else if (seriesItem.getStarLevel() == 3) {
                    vo.setItem3Star(seriesItem);
                } else if (seriesItem.getStarLevel() == 4) {
                    vo.setItem4Star(seriesItem);
                }
            }
        }
        
        return vo;
    }

    /**
     * 查询材料一览列表
     * 
     * @param srItem 材料一览
     * @return 材料一览
     */
    @Override
    public List<SrItem> selectSrItemList(SrItem srItem)
    {
        if (isEmptyQuery(srItem))
        {
            SrItemListCache cached = (SrItemListCache) redisTemplate.opsForValue().get(ITEM_LIST_CACHE_KEY);
            if (cached != null)
            {
                log.debug("selectSrItemList 命中缓存");
                return cached.getList();
            }
            List<SrItem> list = srItemMapper.selectSrItemList(srItem);
            redisTemplate.opsForValue().set(ITEM_LIST_CACHE_KEY, new SrItemListCache(list), SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
            return list;
        }
        return srItemMapper.selectSrItemList(srItem);
    }

    /**
     * 新增材料一览
     * 
     * @param srItem 材料一览
     * @return 结果
     */
    @Override
    public int insertSrItem(SrItem srItem)
    {
        srItem.setCreateTime(DateUtils.getNowDate());
        // 防御性：仅当未预设时才使用当前时间戳；批量场景由调用方预设唯一值避免重复
        if (srItem.getSortOrder() == null) {
            srItem.setSortOrder(System.currentTimeMillis());
        }
        int result = srItemMapper.insertSrItem(srItem);
        clearItemListCache();
        return result;
    }

    /**
     * 修改材料一览
     * 
     * @param srItem 材料一览
     * @return 结果
     */
    @Override
    public int updateSrItem(SrItem srItem)
    {
        srItem.setUpdateTime(DateUtils.getNowDate());
        int result = srItemMapper.updateSrItem(srItem);
        
        // 应用层同步：更新物品信息到材料绑定表
        materialBindSyncService.syncItemInfo(
                srItem.getId(),
                srItem.getItemName(),
                srItem.getImage(),
                srItem.getStarLevel() != null ? srItem.getStarLevel().intValue() : null
        );
        
        // 如果 seriesId 发生变化，同步更新
        if (srItem.getSeriesId() != null) {
            materialBindSyncService.syncItemSeriesId(srItem.getId(), srItem.getSeriesId());
        }

        clearItemListCache();
        return result;
    }
    
    /**
     * 绑定高阶素材
     * 将二星、三星、四星素材的seriesId设置为二星素材的主键ID
     * 
     * @param bindDTO 绑定信息
     * @return 结果
     */
    @Override
    @Transactional
    public int bindAdvancedItems(SrItemBindDTO bindDTO) {
        Long seriesId = bindDTO.getId();
        if (seriesId == null) {
            return 0;
        }
        
        // 设置当前二星素材的seriesId
        SrItem baseItem = new SrItem();
        baseItem.setId(seriesId);
        baseItem.setSeriesId(seriesId);
        baseItem.setUpdateTime(DateUtils.getNowDate());
        srItemMapper.updateSrItem(baseItem);
        
        // 更新三星高阶素材的seriesId
        if (bindDTO.getAdvancedItem3Star() != null) {
            SrItem item3Star = new SrItem();
            item3Star.setId(bindDTO.getAdvancedItem3Star());
            item3Star.setSeriesId(seriesId);
            item3Star.setUpdateTime(DateUtils.getNowDate());
            srItemMapper.updateSrItem(item3Star);
        }
        
        // 更新四星高阶素材的seriesId
        if (bindDTO.getAdvancedItem4Star() != null) {
            SrItem item4Star = new SrItem();
            item4Star.setId(bindDTO.getAdvancedItem4Star());
            item4Star.setSeriesId(seriesId);
            item4Star.setUpdateTime(DateUtils.getNowDate());
            srItemMapper.updateSrItem(item4Star);
        }
        
        return 1;
    }

    /**
     * 批量删除材料一览
     * 
     * @param ids 需要删除的材料一览主键
     * @return 结果
     */
    @Override
    public int deleteSrItemByIds(Long[] ids)
    {
        int result = srItemMapper.deleteSrItemByIds(ids);
        clearItemListCache();
        return result;
    }

    /**
     * 删除材料一览信息
     *
     * @param id 材料一览主键
     * @return 结果
     */
    @Override
    public int deleteSrItemById(Long id)
    {
        return srItemMapper.deleteSrItemById(id);
    }

    /**
     * 批量新增材料一览（统一预设 sortOrder 避免同毫秒重复）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveBatch(List<SrItem> itemList) {
        List<Long> itemIds = new ArrayList<>();
        // 批量场景统一预设 sortOrder，避免 insertSrItem 各自调用 currentTimeMillis 在同毫秒内重复
        long baseTimestamp = System.currentTimeMillis();
        for (int i = 0; i < itemList.size(); i++) {
            SrItem item = itemList.get(i);
            if (item.getSortOrder() == null) {
                // baseTimestamp + i 保证同一批内唯一；i 远小于 1000，不会与下一毫秒冲突
                item.setSortOrder(baseTimestamp + i);
            }
            srItemMapper.insertSrItem(item);
            itemIds.add(item.getId());
        }
        return itemIds;
    }

    /**
     * 导入材料数据（按 itemName 去重）
     * 参考 SrCharacterServiceImpl.importCharacter / SrGachaRecordServiceImpl.importGachaRecord 写法。
     * 只处理 sr_item 主表，不调用 bindAdvancedItems（高阶素材绑定属于独立语义，可导入后用 PUT 接口单独配置）。
     *
     * @param list          从 Excel 解析的材料列表
     * @param updateSupport 已存在时是否更新（false=跳过）
     * @param operName      操作人ID（保留参数以与 RuoYi 标准接口签名一致，便于异步导入扩展）
     * @return 导入结果消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importItem(List<SrItem> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        List<SrItem> toInsert = new ArrayList<>();
        List<SrItem> toUpdate = new ArrayList<>();
        int skipNum = 0;

        for (SrItem item : list)
        {
            // 校验必填字段：物品名字
            if (StringUtils.isEmpty(item.getItemName()))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、物品名字不能为空");
                continue;
            }

            // 校验实装版本：允许为空；非空时必须命中字典 sr_release_version 的有效 dictValue
            // Excel 数字单元格中的 "2.0" 经 POI 整数格式化后会丢失小数变为 "2"，
            // 校验前先按字典把数值形态规整回文本形态（"2" → "2.0"），版本全程按文本处理
            item.setReleaseVersion(matchVersionByDict(item.getReleaseVersion()));
            if (StringUtils.isNotEmpty(item.getReleaseVersion())
                    && StringUtils.isEmpty(DictUtils.getDictLabel("sr_release_version", item.getReleaseVersion())))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、材料[").append(item.getItemName()).append("]实装版本错误，导入失败");
                continue;
            }

            // 按物品名字精确查询是否已存在
            SrItem existing = srItemMapper.selectSrItemByName(item.getItemName());

            if (existing != null)
            {
                if (updateSupport)
                {
                    // 复用已存在记录的 id，走 updateSrItem 更新路径
                    item.setId(existing.getId());
                    toUpdate.add(item);
                }
                else
                {
                    skipNum++;
                }
            }
            else
            {
                // 新增：清空 id 防止 Excel 中误带
                item.setId(null);
                toInsert.add(item);
            }
        }

        // saveBatch 内部统一预设 sortOrder，避免批量插入时同毫秒重复
        int successNum = 0;
        int updateNum = 0;
        if (!toInsert.isEmpty())
        {
            saveBatch(toInsert);
            successNum = toInsert.size();
        }
        if (!toUpdate.isEmpty())
        {
            for (SrItem item : toUpdate)
            {
                updateSrItem(item);
                updateNum++;
            }
        }

        // 构建返回消息
        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("导入完成：新增 ").append(successNum).append(" 条");
        if (updateNum > 0)
        {
            resultMsg.append("，更新 ").append(updateNum).append(" 条");
        }
        if (skipNum > 0)
        {
            resultMsg.append("，跳过重复 ").append(skipNum).append(" 条");
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "，" + failureNum + " 条数据格式不正确：");
            resultMsg.append(failureMsg);
        }

        if(successNum >0 || updateNum > 0){
            clearItemListCache();
        }
        return resultMsg.toString();
    }

    /**
     * 按字典将数值形态的版本号规整为文本形态（"2" → "2.0"）
     * Excel 数字单元格中的 "2.0" 经 POI 整数格式化后丢失小数变为 "2"，与字典文本值无法直接匹配，
     * 此处按数值相等在字典 sr_release_version 中找回真实文本值；非数字输入或未命中时原样返回
     *
     * @param version 导入的版本号
     * @return 字典中的文本形态版本号；未命中时返回原值，交由字典校验报错
     */
    private String matchVersionByDict(String version)
    {
        if (StringUtils.isEmpty(version))
        {
            return version;
        }
        try
        {
            BigDecimal input = new BigDecimal(version.trim());
            List<SysDictData> datas = DictUtils.getDictCache("sr_release_version");
            if (datas != null)
            {
                for (SysDictData dict : datas)
                {
                    if (StringUtils.isEmpty(dict.getDictValue()))
                    {
                        continue;
                    }
                    try
                    {
                        if (input.compareTo(new BigDecimal(dict.getDictValue().trim())) == 0)
                        {
                            return dict.getDictValue();
                        }
                    }
                    catch (NumberFormatException ignore)
                    {
                    }
                }
            }
        }
        catch (NumberFormatException ignore)
        {
        }
        return version;
    }

    /**
     * 清除物品列表相关缓存（前缀下的全部 key）
     */
    private void clearItemListCache()
    {
        Set<Object> keys = redisTemplate.keys(SrhelperCacheConstants.ITEM_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty())
        {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 判断是否为无过滤条件的列表查询
     */
    private boolean isEmptyQuery(SrItem srItem)
    {
        return srItem == null
                || (StringUtils.isEmpty(srItem.getItemName())
                    && StringUtils.isEmpty(srItem.getItemType())
                    // && StringUtils.isEmpty(srItem.getItemTag())
                    && StringUtils.isEmpty(srItem.getDescription())
                    && StringUtils.isEmpty(srItem.getReleaseVersion())
                    && srItem.getStarLevel() == null
                    && srItem.getId() == null
                    && srItem.getSeriesId() == null);
    }
}
