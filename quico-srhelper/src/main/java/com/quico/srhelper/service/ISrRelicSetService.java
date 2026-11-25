package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrRelicSet;
import com.quico.srhelper.domain.vo.SrRelicSetDetailVO;

/**
 * 遗器一览Service接口
 * 
 * @author quico
 * @date 2026-11-24
 */
public interface ISrRelicSetService 
{
    /**
     * 查询遗器一览
     * 
     * @param id 遗器一览主键
     * @return 遗器一览
     */
    public SrRelicSet selectSrRelicSetById(Long id);

    /**
     * 查询遗器套装详情（含套装下的遗器部件）
     * 
     * @param id 遗器一览主键
     * @return 遗器套装详情
     */
    public SrRelicSetDetailVO selectSrRelicSetDetail(Long id);

    /**
     * 查询遗器一览列表
     * 
     * @param srRelicSet 遗器一览
     * @return 遗器一览集合
     */
    public List<SrRelicSet> selectSrRelicSetList(SrRelicSet srRelicSet);

    /**
     * 新增遗器一览
     * 
     * @param srRelicSet 遗器一览
     * @return 结果
     */
    public int insertSrRelicSet(SrRelicSet srRelicSet);

    /**
     * 修改遗器一览
     * 
     * @param srRelicSet 遗器一览
     * @return 结果
     */
    public int updateSrRelicSet(SrRelicSet srRelicSet);

    /**
     * 批量删除遗器一览
     * 
     * @param ids 需要删除的遗器一览主键集合
     * @return 结果
     */
    public int deleteSrRelicSetByIds(Long[] ids);

    /**
     * 删除遗器一览信息
     * 
     * @param id 遗器一览主键
     * @return 结果
     */
    public int deleteSrRelicSetById(Long id);
}
