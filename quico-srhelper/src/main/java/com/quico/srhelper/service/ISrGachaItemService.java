package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrGachaItem;
import com.quico.srhelper.domain.vo.GachaItemBindOptionVO;

/**
 * 卡池项目Service接口
 * 
 * @author quico
 * @date 2026-08-10
 */
public interface ISrGachaItemService 
{
    /**
     * 查询卡池项目
     * 
     * @param id 卡池项目主键
     * @return 卡池项目
     */
    public SrGachaItem selectSrGachaItemById(Long id);

    /**
     * 查询卡池项目列表
     * 
     * @param srGachaItem 卡池项目
     * @return 卡池项目集合
     */
    public List<SrGachaItem> selectSrGachaItemList(SrGachaItem srGachaItem);

    /**
     * 查询尚未绑定卡池项目的角色/光锥候选
     *
     * @param itemType 项目类型：character / lightcone
     * @return 候选集合
     */
    public List<GachaItemBindOptionVO> selectUnboundBindOptions(String itemType);

    /**
     * 新增卡池项目
     * 
     * @param srGachaItem 卡池项目
     * @return 结果
     */
    public int insertSrGachaItem(SrGachaItem srGachaItem);

    /**
     * 修改卡池项目
     * 
     * @param srGachaItem 卡池项目
     * @return 结果
     */
    public int updateSrGachaItem(SrGachaItem srGachaItem);

    /**
     * 批量删除卡池项目
     * 
     * @param ids 需要删除的卡池项目主键集合
     * @return 结果
     */
    public int deleteSrGachaItemByIds(Long[] ids);

    /**
     * 删除卡池项目信息
     * 
     * @param id 卡池项目主键
     * @return 结果
     */
    public int deleteSrGachaItemById(Long id);
}
