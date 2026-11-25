package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrRelic;
import com.quico.srhelper.domain.vo.SrRelicPieceVO;

/**
 * 遗器图鉴Mapper接口
 * 
 * @author quico
 * @date 2026-11-24
 */
public interface SrRelicMapper 
{
    /**
     * 查询遗器图鉴
     * 
     * @param id 遗器图鉴主键
     * @return 遗器图鉴
     */
    public SrRelic selectSrRelicById(Long id);

    /**
     * 按遗器业务ID查询遗器定义
     * 
     * @param relicId 遗器业务ID
     * @return 遗器图鉴
     */
    public SrRelic selectSrRelicByRelicId(String relicId);

    /**
     * 查询遗器图鉴列表
     * 
     * @param srRelic 遗器图鉴
     * @return 遗器图鉴集合
     */
    public List<SrRelic> selectSrRelicList(SrRelic srRelic);

    /**
     * 查询套装下的五星遗器部件列表
     * 
     * @param setId 套装业务ID
     * @return 遗器部件集合
     */
    public List<SrRelicPieceVO> selectRelicPiecesBySetId(String setId);

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
     * 删除遗器图鉴
     * 
     * @param id 遗器图鉴主键
     * @return 结果
     */
    public int deleteSrRelicById(Long id);

    /**
     * 批量删除遗器图鉴
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrRelicByIds(Long[] ids);
}
