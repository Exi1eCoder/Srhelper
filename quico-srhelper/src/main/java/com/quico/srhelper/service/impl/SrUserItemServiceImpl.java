package com.quico.srhelper.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.quico.common.exception.BusinessException;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.service.ISrUserItemService;
import com.quico.srhelper.service.helper.UsableChecker;

/**
 * 用户持有材料Service业务层处理
 * 
 * @author quico
 * @date 2026-06-03
 */
@Service
public class SrUserItemServiceImpl implements ISrUserItemService 
{
    @Autowired
    private SrUserItemMapper srUserItemMapper;

    @Autowired
    private SrItemMapper srItemMapper;

    @Autowired
    private UsableChecker usableChecker;

    /**
     * 查询用户持有材料
     * 
     * @param id 用户持有材料主键
     * @return 用户持有材料
     */
    @Override
    public SrUserItem selectSrUserItemById(Long id)
    {
        return srUserItemMapper.selectSrUserItemById(id);
    }

    /**
     * 查询用户持有材料列表
     * 
     * @param srUserItem 用户持有材料
     * @return 用户持有材料
     */
    @Override
    public List<SrUserItem> selectSrUserItemList(SrUserItem srUserItem)
    {
        List<SrUserItem> list = srUserItemMapper.selectSrUserItemList(srUserItem);
        markUsable(list);
        return list;
    }

    /**
     * 按图鉴材料绑定完成度标记可用性（未完成绑定的 CAL/TRA 不可在“我的材料”展示与删减库存）
     */
    private void markUsable(List<SrUserItem> list)
    {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> itemIds = list.stream().map(SrUserItem::getItemId).collect(Collectors.toSet());
        Set<Long> unusableItemIds = findUnusableItems(itemIds).stream()
                .map(SrItem::getId).collect(Collectors.toSet());
        for (SrUserItem userItem : list) {
            userItem.setUsable(!unusableItemIds.contains(userItem.getItemId()));
        }
    }

    /**
     * 新增用户持有材料
     * 
     * @param srUserItem 用户持有材料
     * @return 结果
     */
    @Override
    public int insertSrUserItem(SrUserItem srUserItem)
    {
        srUserItem.setCreateTime(DateUtils.getNowDate());
        return srUserItemMapper.insertSrUserItem(srUserItem);
    }

    /**
     * 批量提交用户持有材料
     * 
     * 数量>0 合并为一条 Upsert（insert ... on duplicate key update）；
     * 数量<=0 按用户分组物理删除，主表只保留当前拥有的数据。
     * 
     * @param list 用户持有材料集合
     * @return 处理条数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertOrUpdate(List<SrUserItem> list)
    {
        if (list == null || list.isEmpty()) {
            throw BusinessException.badRequest("批量数据不能为空");
        }

        // 1. 按 (userId, itemId) 去重（同键以最后一条为准）；userId 缺失时取当前登录用户，其余非法数据直接返回 400
        Long loginUserId = SecurityUtils.getUserId();
        Map<String, SrUserItem> distinctMap = new LinkedHashMap<>();
        int index = 0;
        for (SrUserItem item : list) {
            index++;
            if (item == null) {
                throw BusinessException.badRequest("第" + index + "条数据不能为空");
            }
            if (item.getUserId() == null) {
                item.setUserId(loginUserId);
            }
            if (item.getUserId() == null) {
                throw BusinessException.badRequest("第" + index + "条数据 userId 不能为空");
            }
            if (item.getItemId() == null) {
                throw BusinessException.badRequest("第" + index + "条数据 itemId 不能为空");
            }
            if (item.getQuantity() == null) {
                throw BusinessException.badRequest("第" + index + "条数据 quantity 不能为空");
            }
            distinctMap.put(pairKey(item.getUserId(), item.getItemId()), item);
        }

        // 2. 兜底校验：CAL/TRA 系列高阶素材绑定未完成的材料不允许录入或删减库存
        Set<Long> submittedItemIds = distinctMap.values().stream()
                .map(SrUserItem::getItemId)
                .collect(Collectors.toSet());
        assertItemsUsable(submittedItemIds);

        // 3. 拆分：数量>0 走 Upsert，数量<=0 走物理删除
        List<SrUserItem> upsertList = new ArrayList<>();
        Map<Long, List<Long>> deleteItemIdsByUser = new HashMap<>();
        for (SrUserItem item : distinctMap.values()) {
            if (item.getQuantity() > 0) {
                upsertList.add(item);
            } else {
                deleteItemIdsByUser.computeIfAbsent(item.getUserId(), k -> new ArrayList<>()).add(item.getItemId());
            }
        }

        if (!upsertList.isEmpty()) {
            srUserItemMapper.batchUpsert(upsertList);
        }
        for (Map.Entry<Long, List<Long>> entry : deleteItemIdsByUser.entrySet()) {
            srUserItemMapper.deleteByUserIdAndItemIds(entry.getKey(), entry.getValue());
        }
        // 返回处理条数：Upsert 在“值未变化”时受影响行数为 0，不能据此判定失败
        return distinctMap.size();
    }

    /**
     * 构造 (userId, itemId) 组合键
     */
    private String pairKey(Long userId, Long itemId)
    {
        return userId + "#" + itemId;
    }

    /**
     * 校验提交的材料是否都属于可用材料（CAL/TRA 系列必须绑定完整）
     * 命中未完成绑定即抛 422 阻断，防止绕过前端直接录入或删减库存
     *
     * @param itemIds 本批提交涉及的材料ID集合
     */
    private void assertItemsUsable(Set<Long> itemIds)
    {
        List<SrItem> invalidItems = findUnusableItems(itemIds);
        if (!invalidItems.isEmpty()) {
            String names = invalidItems.stream().map(SrItem::getItemName).collect(Collectors.joining("、"));
            throw BusinessException.unprocessable(
                    "材料[" + names + "]高阶素材绑定未完成，不可录入或删减库存");
        }
    }

    /**
     * 查询未完成高阶素材绑定的材料（CAL/TRA 系列不完整），返回空表示全部可用
     *
     * @param itemIds 待校验的材料ID集合
     * @return 未完成绑定的材料集合
     */
    private List<SrItem> findUnusableItems(Collection<Long> itemIds)
    {
        if (itemIds == null || itemIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SrItem> items = srItemMapper.selectSrItemByIds(new ArrayList<>(itemIds));
        Set<Long> completeSeriesIds = usableChecker.getCompleteSeriesIds();
        List<SrItem> invalidItems = new ArrayList<>();
        for (SrItem item : items) {
            if (!usableChecker.isItemUsable(item, completeSeriesIds)) {
                invalidItems.add(item);
            }
        }
        return invalidItems;
    }

    /**
     * 修改用户持有材料
     * 
     * @param srUserItem 用户持有材料
     * @return 结果
     */
    @Override
    public int updateSrUserItem(SrUserItem srUserItem)
    {
        srUserItem.setUpdateTime(DateUtils.getNowDate());
        return srUserItemMapper.updateSrUserItem(srUserItem);
    }

    /**
     * 批量删除用户持有材料
     * 
     * @param ids 需要删除的用户持有材料主键
     * @return 结果
     */
    @Override
    public int deleteSrUserItemByIds(Long[] ids)
    {
        return srUserItemMapper.deleteSrUserItemByIds(ids);
    }

    /**
     * 删除用户持有材料信息
     * 
     * @param id 用户持有材料主键
     * @return 结果
     */
    @Override
    public int deleteSrUserItemById(Long id)
    {
        return srUserItemMapper.deleteSrUserItemById(id);
    }

    /**
     * 获取用户持有材料数量映射
     * 
     * @param userId 用户ID
     * @return Map<itemId, quantity>
     */
    @Override
    public Map<Long, Long> getQuantityMapByUserId(Long userId) {
        Map<Long, Long> quantityMap = new HashMap<>();
        SrUserItem query = new SrUserItem();
        query.setUserId(userId);
        List<SrUserItem> items = srUserItemMapper.selectSrUserItemList(query);
        for (SrUserItem item : items) {
            quantityMap.put(item.getItemId(), item.getQuantity());
        }
        return quantityMap;
    }

    /**
     * 根据用户ID和材料ID查询记录
     * 
     * @param userId 用户ID
     * @param itemId 材料ID
     * @return 用户持有材料
     */
    @Override
    public SrUserItem selectByUserIdAndItemId(Long userId, Long itemId) {
        return srUserItemMapper.selectByUserIdAndItemId(userId, itemId);
    }
}