package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.dto.SrItemBindDTO;
import com.quico.srhelper.domain.vo.SrItemDetailVO;

/**
 * 材料一览Service接口
 * 
 * @author quico
 * @date 2026-05-25
 */
public interface ISrItemService 
{
    /**
     * 查询材料一览
     * 
     * @param id 材料一览主键
     * @return 材料一览
     */
    public SrItem selectSrItemById(Long id);

    /**
     * 查询材料详情（包含相关高阶素材）
     * 
     * @param id 材料一览主键
     * @return 材料详情VO
     */
    public SrItemDetailVO selectSrItemDetailById(Long id);

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
     * 批量删除材料一览
     * 
     * @param ids 需要删除的材料一览主键集合
     * @return 结果
     */
    public int deleteSrItemByIds(Long[] ids);

    /**
     * 删除材料一览信息
     * 
     * @param id 材料一览主键
     * @return 结果
     */
    public int deleteSrItemById(Long id);

    /**
     * 绑定高阶素材
     *
     * @param bindDTO 绑定信息
     * @return 结果
     */
    public int bindAdvancedItems(SrItemBindDTO bindDTO);

    /**
     * 批量新增材料一览（统一预设 sortOrder 避免同毫秒重复）
     *
     * @param itemList 材料一览列表
     * @return 新增后的主键ID列表
     */
    List<Long> saveBatch(List<SrItem> itemList);

    /**
     * 导入材料数据（按 itemName 去重）
     *
     * @param list          从 Excel 解析的材料列表
     * @param updateSupport 已存在时是否更新（false=跳过）
     * @param operName      操作人ID
     * @return 导入结果消息
     */
    String importItem(List<SrItem> list, boolean updateSupport, String operName);
}
