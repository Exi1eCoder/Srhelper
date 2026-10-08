package com.quico.srhelper.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.quico.common.core.domain.entity.SysDictData;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.DictUtils;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.mapper.SrLightConesMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import com.quico.srhelper.domain.cache.SrLightConesListCache;
import com.quico.srhelper.domain.dto.LightConeSaveDTO;
import com.quico.srhelper.service.ISrLightConesService;
import com.quico.srhelper.service.helper.MaterialBindExpander;
import com.quico.srhelper.service.helper.MaterialBindSyncService;
import com.quico.srhelper.service.helper.LightconeAscensionMaterialGenerator;
import com.quico.srhelper.config.GachaRecordImageCacheManager;
import com.quico.srhelper.config.SrhelperCacheConstants;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * 光锥一览Service业务层处理
 * 
 * @author quico
 * @date 2026-05-25
 */
@Service
@Slf4j
public class SrLightConesServiceImpl implements ISrLightConesService 
{
    @Autowired
    private SrLightConesMapper srLightConesMapper;

    @Autowired
    private SrLightconeMaterialBindMapper materialBindMapper;

    @Autowired
    private MaterialBindSyncService materialBindSyncService;

    @Autowired
    private MaterialBindExpander materialBindExpander;

    @Autowired
    private LightconeAscensionMaterialGenerator ascensionGenerator;

    @Autowired
    private GachaRecordImageCacheManager imageCacheManager;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /** 光锥列表默认查询缓存 key（无过滤条件时的分页列表） */
    private static final String LIGHTCONE_LIST_CACHE_KEY = SrhelperCacheConstants.LIGHTCONE_LIST_KEY + "list";

    /**
     * 查询光锥一览
     *
     * @param id 光锥一览主键
     * @return 光锥一览
     */
    @Override
    public SrLightCones selectSrLightConesById(Long id)
    {
        return srLightConesMapper.selectSrLightConesById(id);
    }

    /**
     * 查询光锥一览列表
     * 
     * @param srLightCones 光锥一览
     * @return 光锥一览
     */
    @Override
    public List<SrLightCones> selectSrLightConesList(SrLightCones srLightCones)
    {
        if (isEmptyQuery(srLightCones))
        {
            SrLightConesListCache cached = (SrLightConesListCache) redisTemplate.opsForValue().get(LIGHTCONE_LIST_CACHE_KEY);
            if (cached != null)
            {
                log.debug("selectSrLightConesList 命中缓存");
                return cached.getList();
            }
            List<SrLightCones> list = srLightConesMapper.selectSrLightConesList(srLightCones);
            redisTemplate.opsForValue().set(LIGHTCONE_LIST_CACHE_KEY, new SrLightConesListCache(list), SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
            return list;
        }
        return srLightConesMapper.selectSrLightConesList(srLightCones);
    }

    /**
     * 新增光锥一览
     * 
     * @param srLightCones 光锥一览
     * @return 结果
     */
    @Override
    public int insertSrLightCones(SrLightCones srLightCones)
    {
        srLightCones.setCreateTime(DateUtils.getNowDate());
        // 防御性：仅当未预设时才使用当前时间戳；批量场景由调用方预设唯一值避免重复
        if (srLightCones.getSortOrder() == null) {
            srLightCones.setSortOrder(System.currentTimeMillis());
        }
        int result = srLightConesMapper.insertSrLightCones(srLightCones);
        clearLightConeListCache();
        return result;
    }

    /**
     * 修改光锥一览
     * 
     * @param srLightCones 光锥一览
     * @return 结果
     */
    @Override
    public int updateSrLightCones(SrLightCones srLightCones)
    {
        srLightCones.setUpdateTime(DateUtils.getNowDate());
        int result = srLightConesMapper.updateSrLightCones(srLightCones);
        clearLightConeListCache();
        return result;
    }

    /**
     * 批量删除光锥一览
     * 
     * @param ids 需要删除的光锥一览主键
     * @return 结果
     */
    @Override
    public int deleteSrLightConesByIds(Long[] ids)
    {
        int result = srLightConesMapper.deleteSrLightConesByIds(ids);
        clearLightConeListCache();
        return result;
    }

    /**
     * 删除光锥一览信息
     * 
     * @param id 光锥一览主键
     * @return 结果
     */
    @Override
    public int deleteSrLightConesById(Long id)
    {
        return srLightConesMapper.deleteSrLightConesById(id);
    }

    /**
     * 统一保存/更新光锥及材料绑定
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAll(LightConeSaveDTO dto) {
        Long lightConeId;
        Date now = DateUtils.getNowDate();

        // 1. 保存/更新光锥基本信息
        String currentUser = SecurityUtils.getUserId().toString();
        if (dto.getLightCone().getId() == null) {
            dto.getLightCone().setCreateBy(currentUser);
            dto.getLightCone().setUpdateBy(currentUser);
            dto.getLightCone().setCreateTime(now);
            dto.getLightCone().setUpdateTime(now);
            dto.getLightCone().setSortOrder(System.currentTimeMillis());
            srLightConesMapper.insertSrLightCones(dto.getLightCone());
            lightConeId = dto.getLightCone().getId();
        } else {
            lightConeId = dto.getLightCone().getId();
            dto.getLightCone().setUpdateBy(currentUser);
            dto.getLightCone().setUpdateTime(now);
            srLightConesMapper.updateSrLightCones(dto.getLightCone());
            // 应用层同步：更新角色名称到材料绑定表
            materialBindSyncService.syncLightconeName(dto.getLightCone().getId(), dto.getLightCone().getLightConeName());
            log.info("changeType============: {}", dto.getChangeType());
            log.info("currentUser============: {}",currentUser);
        }

        // 刷新卡池项目图片缓存
        imageCacheManager.refresh();

        if("simple".equals(dto.getChangeType()) || dto.getMaterialBinds() == null){
            clearLightConeListCache();
            return lightConeId;
        }

        // 2. 保存材料绑定（先删后插，TRA/CAL通过seriesId扩展高阶素材）
        if (dto.getMaterialBinds() != null) {
            materialBindMapper.deleteByLightConeId(lightConeId);
            int sortOrder = 0;
            for (SrLightconeMaterialBind bind : dto.getMaterialBinds()) {
                // TRA/CAL 类型扩展为同系列全部稀有度
                if (materialBindExpander.isExpandable(bind.getItemAscensionType()) && bind.getSeriesId() != null) {
                    List<SrItem> seriesItems = materialBindExpander.expandBySeriesId(bind.getSeriesId());
                    for (SrItem item : seriesItems) {
                        SrLightconeMaterialBind expanded = SrLightconeMaterialBind.builder()
                                .lightconeId(dto.getLightCone().getId())
                                .lightconeName(dto.getLightCone().getLightConeName())
                                .itemAscensionType(bind.getItemAscensionType())
                                .itemId(item.getId())
                                .seriesId(bind.getSeriesId())
                                .itemName(item.getItemName())
                                .itemImage(item.getImage())
                                .rarityLevel(item.getStarLevel().intValue())
                                .sortOrder(sortOrder++)
                                .build();
                        expanded.setCreateBy(currentUser);
                        expanded.setUpdateBy(currentUser);
                        expanded.setCreateTime(now);
                        expanded.setUpdateTime(now);
                        materialBindMapper.insert(expanded);
                    }
                } else {
                    bind.setLightconeId(lightConeId);
                    bind.setLightconeName(dto.getLightCone().getLightConeName());
                    bind.setSortOrder(sortOrder++);
                    bind.setCreateBy(currentUser);
                    bind.setUpdateBy(currentUser);
                    bind.setCreateTime(now);
                    bind.setUpdateTime(now);
                    materialBindMapper.insert(bind);
                }
            }
            // 3. 根据模板自动生成晋升材料
            ascensionGenerator.generateAndSave(lightConeId, dto.getLightCone(), dto.getMaterialBinds());
        }

        clearLightConeListCache();
        return lightConeId;
    }

    @Override
    public LightConeSaveDTO getDetail(Long id) {
        SrLightCones lightCone = srLightConesMapper.selectSrLightConesById(id);
        List<SrLightconeMaterialBind> binds = materialBindMapper.selectByLightConeId(id);
        LightConeSaveDTO dto = new LightConeSaveDTO();
        dto.setLightCone(lightCone);
        dto.setMaterialBinds(binds);
        return dto;
    }

    /**
     * 批量新增光锥（统一预设 sortOrder 避免同毫秒重复）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveBatch(List<SrLightCones> lightConesList) {
        List<Long> ids = new ArrayList<>();
        long baseTimestamp = System.currentTimeMillis();
        for (int i = 0; i < lightConesList.size(); i++) {
            SrLightCones lc = lightConesList.get(i);
            lc.setCreateTime(DateUtils.getNowDate());
            if (lc.getSortOrder() == null) {
                // baseTimestamp + i 保证同一批内唯一；i 远小于 1000，不会与下一毫秒冲突
                lc.setSortOrder(baseTimestamp + i);
            }
            srLightConesMapper.insertSrLightCones(lc);
            ids.add(lc.getId());
        }
        return ids;
    }

    /**
     * 导入光锥数据（按光锥名称 + 实装版本去重）
     * 参考 SrCharacterServiceImpl.importCharacter / SrItemServiceImpl.importItem 写法。
     * 只处理 sr_lightcone 主表，不处理材料绑定（可导入后用 saveAll/PUT 接口单独配置）。
     *
     * @param list          从 Excel 解析的光锥列表
     * @param updateSupport 已存在时是否更新（false=跳过）
     * @param operName      操作人ID
     * @return 导入结果消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importLightCones(List<SrLightCones> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        List<SrLightCones> toInsert = new ArrayList<>();
        List<SrLightCones> toUpdate = new ArrayList<>();
        int skipNum = 0;

        for (SrLightCones lc : list)
        {
            // 校验必填字段：光锥名称
            if (StringUtils.isEmpty(lc.getLightConeName()))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、光锥名称不能为空");
                continue;
            }

            // 校验实装版本：允许为空；非空时必须命中字典 sr_release_version 的有效 dictValue
            // Excel 数字单元格中的 "2.0" 经 POI 整数格式化后会丢失小数变为 "2"，
            // 校验前先按字典把数值形态规整回文本形态（"2" → "2.0"），版本全程按文本处理
            lc.setReleaseVersion(matchVersionByDict(lc.getReleaseVersion()));
            if (StringUtils.isNotEmpty(lc.getReleaseVersion())
                    && StringUtils.isEmpty(DictUtils.getDictLabel("sr_release_version", lc.getReleaseVersion())))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、光锥[").append(lc.getLightConeName()).append("]实装版本错误，导入失败");
                continue;
            }

            // 按光锥名称 + 实装版本精确查询是否已存在
            SrLightCones existing = srLightConesMapper.selectSrLightConesByNameAndVersion(
                    lc.getLightConeName(), lc.getReleaseVersion());

            if (existing != null)
            {
                if (updateSupport)
                {
                    // 复用已存在记录的 id，走 updateSrLightCones 更新路径
                    lc.setId(existing.getId());
                    toUpdate.add(lc);
                }
                else
                {
                    skipNum++;
                }
            }
            else
            {
                // 新增：清空 id 防止 Excel 中误带
                lc.setId(null);
                toInsert.add(lc);
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
            for (SrLightCones lc : toUpdate)
            {
                updateSrLightCones(lc);
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

        // 有成功写入或更新时清除光锥列表缓存
        if (successNum > 0 || updateNum > 0)
        {
            clearLightConeListCache();
        }
        return resultMsg.toString();
    }

    /**
     * 清除光锥列表相关缓存（前缀下的全部 key）
     */
    private void clearLightConeListCache()
    {
        Set<Object> keys = redisTemplate.keys(SrhelperCacheConstants.LIGHTCONE_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty())
        {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 判断是否为无过滤条件的列表查询（分页参数不影响，只关注业务字段）
     */
    private boolean isEmptyQuery(SrLightCones srLightCones)
    {
        return srLightCones == null
                || (StringUtils.isEmpty(srLightCones.getLightConeName())
                    && StringUtils.isEmpty(srLightCones.getPath())
                    && StringUtils.isEmpty(srLightCones.getTag())
                    && StringUtils.isEmpty(srLightCones.getReleaseVersion())
                    && StringUtils.isEmpty(srLightCones.getDescription())
                    && srLightCones.getStarLevel() == null
                    && srLightCones.getId() == null);
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
}
