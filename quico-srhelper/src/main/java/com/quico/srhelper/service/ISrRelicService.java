package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrRelic;
import com.quico.srhelper.domain.vo.RelicRuleDataVO;

/**
 * 遗器图鉴Service接口
 * 
 * @author quico
 * @date 2026-11-24
 */
public interface ISrRelicService 
{
    /**
     * 查询遗器图鉴
     * 
     * @param id 遗器图鉴主键
     * @return 遗器图鉴
     */
    public SrRelic selectSrRelicById(Long id);

    /**
     * 查询遗器图鉴列表
     * 
     * @param srRelic 遗器图鉴
     * @return 遗器图鉴集合
     */
    public List<SrRelic> selectSrRelicList(SrRelic srRelic);

    /**
     * 查询前端所需的遗器规则数据（仅 5 星）
     * 
     * @return 规则数据
     */
    public RelicRuleDataVO selectRelicRuleData();

    /**
     * 新增遗器图鉴
     * 
     * @param srRelic 遗器图鉴
     * @return 结果
     */
    public int insertSrRelic(SrRelic srRelic);

    /**
     * 修改遗器图鉴
     * 
     * @param srRelic 遗器图鉴
     * @return 结果
     */
    public int updateSrRelic(SrRelic srRelic);

    /**
     * 批量删除遗器图鉴
     * 
     * @param ids 需要删除的遗器图鉴主键集合
     * @return 结果
     */
    public int deleteSrRelicByIds(Long[] ids);

    /**
     * 删除遗器图鉴信息
     * 
     * @param id 遗器图鉴主键
     * @return 结果
     */
    public int deleteSrRelicById(Long id);
}
