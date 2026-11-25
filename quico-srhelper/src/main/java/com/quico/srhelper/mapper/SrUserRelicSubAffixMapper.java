package com.quico.srhelper.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quico.srhelper.domain.SrUserRelicSubAffix;

/**
 * 用户遗器副词条Mapper接口
 * 
 * @author quico
 * @date 2026-11-25
 */
public interface SrUserRelicSubAffixMapper
{
    /**
     * 查询某件用户遗器的副词条
     * 
     * @param userRelicId 用户遗器主键
     * @return 副词条集合
     */
    public List<SrUserRelicSubAffix> selectByUserRelicId(Long userRelicId);

    /**
     * 批量新增副词条
     * 
     * @param list 副词条集合
     * @return 结果
     */
    public int batchInsert(List<SrUserRelicSubAffix> list);

    /**
     * 删除某件用户遗器的全部副词条
     * 
     * @param userRelicId 用户遗器主键
     * @return 结果
     */
    public int deleteByUserRelicId(Long userRelicId);

    /**
     * 按用户遗器主键批量删除副词条
     * 
     * @param ids 用户遗器主键集合
     * @return 结果
     */
    public int deleteByUserRelicIds(@Param("ids") Long[] ids);
}
