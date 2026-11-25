package com.quico.srhelper.mapper;

import java.util.List;

import com.quico.srhelper.domain.SrRelicSetProperty;

/**
 * 遗器套装静态属性Mapper接口
 * 
 * @author quico
 * @date 2026-11-25
 */
public interface SrRelicSetPropertyMapper
{
    /**
     * 查询全部套装静态属性
     * 
     * @return 套装静态属性集合
     */
    public List<SrRelicSetProperty> selectSrRelicSetPropertyList();
}
