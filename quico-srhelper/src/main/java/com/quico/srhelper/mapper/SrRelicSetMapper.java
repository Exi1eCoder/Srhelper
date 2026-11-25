package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrRelicSet;

/**
 * 遗器一览Mapper接口
 * 
 * @author quico
 * @date 2026-11-24
 */
public interface SrRelicSetMapper 
{
    /**
     * 查询遗器一览
     * 
     * @param id 遗器一览主键
     * @return 遗器一览
     */
    public SrRelicSet selectSrRelicSetById(Long id);

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
     * 删除遗器一览
     * 
     * @param id 遗器一览主键
     * @return 结果
     */
    public int deleteSrRelicSetById(Long id);

    /**
     * 批量删除遗器一览
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrRelicSetByIds(Long[] ids);
}
