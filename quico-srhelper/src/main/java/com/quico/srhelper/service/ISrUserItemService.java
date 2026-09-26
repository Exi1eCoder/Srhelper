package com.quico.srhelper.service;

import java.util.List;
import java.util.Map;
import com.quico.srhelper.domain.SrUserItem;

/**
 * 用户持有材料Service接口
 * 
 * @author quico
 * @date 2026-06-03
 */
public interface ISrUserItemService 
{
    /**
     * 查询用户持有材料
     * 
     * @param id 用户持有材料主键
     * @return 用户持有材料
     */
    public SrUserItem selectSrUserItemById(Long id);

    /**
     * 查询用户持有材料列表
     * 
     * @param srUserItem 用户持有材料
     * @return 用户持有材料集合
     */
    public List<SrUserItem> selectSrUserItemList(SrUserItem srUserItem);

    /**
     * 新增用户持有材料
     * 
     * @param srUserItem 用户持有材料
     * @return 结果
     */
    public int insertSrUserItem(SrUserItem srUserItem);

    /**
     * 修改用户持有材料
     * 
     * @param srUserItem 用户持有材料
     * @return 结果
     */
    public int updateSrUserItem(SrUserItem srUserItem);

    /**
     * 批量删除用户持有材料
     * 
     * @param ids 需要删除的用户持有材料主键集合
     * @return 结果
     */
    public int deleteSrUserItemByIds(Long[] ids);

    /**
     * 删除用户持有材料信息
     * 
     * @param id 用户持有材料主键
     * @return 结果
     */
    public int deleteSrUserItemById(Long id);

    /**
     * 获取用户持有材料数量映射
     * 
     * @param userId 用户ID
     * @return Map<itemId, quantity>
     */
    public Map<Long, Long> getQuantityMapByUserId(Long userId);

    /**
     * 根据用户ID和材料ID查询记录
     * 
     * @param userId 用户ID
     * @param itemId 材料ID
     * @return 用户持有材料
     */
    public SrUserItem selectByUserIdAndItemId(Long userId, Long itemId);
}
