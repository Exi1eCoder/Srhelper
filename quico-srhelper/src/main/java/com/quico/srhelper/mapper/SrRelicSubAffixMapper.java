package com.quico.srhelper.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quico.srhelper.domain.SrRelicSubAffix;

/**
 * 遗器副属性词条Mapper接口
 * 
 * @author quico
 * @date 2026-11-25
 */
public interface SrRelicSubAffixMapper
{
    /**
     * 按词条组查询副属性池
     * 
     * @param groupId 词条组ID
     * @return 副属性词条集合
     */
    public List<SrRelicSubAffix> selectByGroupId(@Param("groupId") String groupId);
}
