package com.quico.srhelper.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quico.srhelper.domain.SrRelicMainAffix;

/**
 * 遗器主属性词条Mapper接口
 * 
 * @author quico
 * @date 2026-11-25
 */
public interface SrRelicMainAffixMapper
{
    /**
     * 按词条组查询主属性池
     * 
     * @param groupIds 词条组ID集合
     * @return 主属性词条集合
     */
    public List<SrRelicMainAffix> selectByGroupIds(@Param("groupIds") List<String> groupIds);
}
