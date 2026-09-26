package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterMaterialBind;

/**
 * 角色材料绑定Service接口
 * 
 * @author quico
 * @date 2026-06-14
 */
public interface ISrCharacterMaterialBindService 
{
    /**
     * 查询角色材料绑定
     * 
     * @param id 角色材料绑定主键
     * @return 角色材料绑定
     */
    public SrCharacterMaterialBind selectSrCharacterMaterialBindById(Long id);

    /**
     * 查询角色材料绑定列表
     * 
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 角色材料绑定集合
     */
    public List<SrCharacterMaterialBind> selectSrCharacterMaterialBindList(SrCharacterMaterialBind srCharacterMaterialBind);

    /**
     * 新增角色材料绑定
     * 
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 结果
     */
    public int insertSrCharacterMaterialBind(SrCharacterMaterialBind srCharacterMaterialBind);

    /**
     * 修改角色材料绑定
     * 
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 结果
     */
    public int updateSrCharacterMaterialBind(SrCharacterMaterialBind srCharacterMaterialBind);

    /**
     * 批量删除角色材料绑定
     * 
     * @param ids 需要删除的角色材料绑定主键集合
     * @return 结果
     */
    public int deleteSrCharacterMaterialBindByIds(Long[] ids);

    /**
     * 删除角色材料绑定信息
     * 
     * @param id 角色材料绑定主键
     * @return 结果
     */
    public int deleteSrCharacterMaterialBindById(Long id);
}
