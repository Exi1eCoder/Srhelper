package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.dto.BindImportResult;
import com.quico.srhelper.domain.dto.SrCharacterMaterialBindExcel;

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

    /**
     * 查询角色材料绑定透视数据（一行一个角色，四类材料各一列，TRA/CAL 取最低稀有度基础材料名）
     *
     * @param query 查询条件
     * @return 透视格式数据
     */
    public List<SrCharacterMaterialBindExcel> selectBindExcelList(SrCharacterMaterialBind query);

    /**
     * 导入角色材料绑定透视数据（按角色名称匹配角色，按材料名称匹配材料）
     *
     * @param list          Excel 解析的透视数据
     * @param updateSupport 角色已存在绑定时是否覆盖（false=跳过）
     * @param operName      操作人ID
     * @return 导入结果（含消息摘要与失败角色名列表）
     */
    public BindImportResult importBindExcel(List<SrCharacterMaterialBindExcel> list, boolean updateSupport, String operName);
}
