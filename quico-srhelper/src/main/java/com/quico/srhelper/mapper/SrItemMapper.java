package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrItem;
import org.apache.ibatis.annotations.Param;

/**
 * 材料一览Mapper接口
 *
 * @author quico
 * @date 2026-05-25
 */
public interface SrItemMapper
{
    /**
     * 查询材料一览
     *
     * @param id 材料一览主键
     * @return 材料一览
     */
    public SrItem selectSrItemById(Long id);

    /**
     * 按主键集合批量查询材料一览
     *
     * @param ids 材料一览主键集合
     * @return 材料一览集合
     */
    public List<SrItem> selectSrItemByIds(@Param("ids") List<Long> ids);

    /**
     * 按物品名字精确查询（导入去重用）
     *
     * @param itemName 物品名字
     * @return 材料一览，无匹配返回 null
     */
    public SrItem selectSrItemByName(@Param("itemName") String itemName);

    /**
     * 查询材料一览列表
     * 
     * @param srItem 材料一览
     * @return 材料一览集合
     */
    public List<SrItem> selectSrItemList(SrItem srItem);

    /**
     * 新增材料一览
     * 
     * @param srItem 材料一览
     * @return 结果
     */
    public int insertSrItem(SrItem srItem);

    /**
     * 修改材料一览
     * 
     * @param srItem 材料一览
     * @return 结果
     */
    public int updateSrItem(SrItem srItem);

    /**
     * 删除材料一览
     * 
     * @param id 材料一览主键
     * @return 结果
     */
    public int deleteSrItemById(Long id);

    /**
     * 批量删除材料一览
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrItemByIds(Long[] ids);

    /**
     * 根据seriesId查询同系列材料
     * 
     * @param seriesId 素材系列ID
     * @return 同系列材料列表
     */
    public List<SrItem> selectBySeriesId(Long seriesId);

    /**
     * 查询高阶素材绑定完整的系列锚点ID集合
     * 以二星 CAL/TRA 行为锚点，要求同系列同类型共3行（二/三/四星）
     * 
     * @return 完整系列锚点ID集合
     */
    public List<Long> selectCompleteSeriesIds();
}
