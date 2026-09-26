package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.service.ISrCharacterMaterialBindService;

/**
 * 角色材料绑定Service业务层处理
 * 
 * @author quico
 * @date 2026-06-14
 */
@Service
public class SrCharacterMaterialBindServiceImpl implements ISrCharacterMaterialBindService 
{
    @Autowired
    private SrCharacterMaterialBindMapper srCharacterMaterialBindMapper;

    /**
     * 查询角色材料绑定
     * 
     * @param id 角色材料绑定主键
     * @return 角色材料绑定
     */
    @Override
    public SrCharacterMaterialBind selectSrCharacterMaterialBindById(Long id)
    {
        return srCharacterMaterialBindMapper.selectSrCharacterMaterialBindById(id);
    }

    /**
     * 查询角色材料绑定列表
     * 
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 角色材料绑定
     */
    @Override
    public List<SrCharacterMaterialBind> selectSrCharacterMaterialBindList(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        return srCharacterMaterialBindMapper.selectSrCharacterMaterialBindList(srCharacterMaterialBind);
    }

    /**
     * 新增角色材料绑定
     * 
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 结果
     */
    @Override
    public int insertSrCharacterMaterialBind(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        srCharacterMaterialBind.setCreateTime(DateUtils.getNowDate());
        return srCharacterMaterialBindMapper.insertSrCharacterMaterialBind(srCharacterMaterialBind);
    }

    /**
     * 修改角色材料绑定
     * 
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 结果
     */
    @Override
    public int updateSrCharacterMaterialBind(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        srCharacterMaterialBind.setUpdateTime(DateUtils.getNowDate());
        return srCharacterMaterialBindMapper.updateSrCharacterMaterialBind(srCharacterMaterialBind);
    }

    /**
     * 批量删除角色材料绑定
     * 
     * @param ids 需要删除的角色材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterMaterialBindByIds(Long[] ids)
    {
        return srCharacterMaterialBindMapper.deleteSrCharacterMaterialBindByIds(ids);
    }

    /**
     * 删除角色材料绑定信息
     * 
     * @param id 角色材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterMaterialBindById(Long id)
    {
        return srCharacterMaterialBindMapper.deleteSrCharacterMaterialBindById(id);
    }
}
