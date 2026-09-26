package com.quico.srhelper.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.quico.srhelper.domain.SrUserItem;

/**
 * 用户持有材料Mapper接口
 * 
 * @author quico
 * @date 2026-06-03
 */
public interface SrUserItemMapper 
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
     * 删除用户持有材料
     * 
     * @param id 用户持有材料主键
     * @return 结果
     */
    public int deleteSrUserItemById(Long id);

    /**
     * 批量删除用户持有材料
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrUserItemByIds(Long[] ids);

    /**
     * 根据用户ID和材料ID查询记录
     * 
     * @param userId 用户ID
     * @param itemId 材料ID
     * @return 用户持有材料
     */
    public SrUserItem selectByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);
}
