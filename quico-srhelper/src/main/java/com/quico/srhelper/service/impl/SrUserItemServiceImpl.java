package com.quico.srhelper.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.service.ISrUserItemService;

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
        return srUserItemMapper.selectSrUserItemList(srUserItem);
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