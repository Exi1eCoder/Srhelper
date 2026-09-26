package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterAscensionTemplate;

/**
 * 角色晋升材料模板Service接口
 * 
 * @author quico
 * @date 2026-06-15
 */
public interface ISrCharacterAscensionTemplateService 
{
    /**
     * 查询角色晋升材料模板
     * 
     * @param id 角色晋升材料模板主键
     * @return 角色晋升材料模板
     */
    public SrCharacterAscensionTemplate selectSrCharacterAscensionTemplateById(Long id);

    /**
     * 查询角色晋升材料模板列表
     * 
     * @param srCharacterAscensionTemplate 角色晋升材料模板
     * @return 角色晋升材料模板集合
     */
    public List<SrCharacterAscensionTemplate> selectSrCharacterAscensionTemplateList(SrCharacterAscensionTemplate srCharacterAscensionTemplate);

    /**
     * 新增角色晋升材料模板
     * 
     * @param srCharacterAscensionTemplate 角色晋升材料模板
     * @return 结果
     */
    public int insertSrCharacterAscensionTemplate(SrCharacterAscensionTemplate srCharacterAscensionTemplate);

    /**
     * 修改角色晋升材料模板
     * 
     * @param srCharacterAscensionTemplate 角色晋升材料模板
     * @return 结果
     */
    public int updateSrCharacterAscensionTemplate(SrCharacterAscensionTemplate srCharacterAscensionTemplate);

    /**
     * 批量删除角色晋升材料模板
     * 
     * @param ids 需要删除的角色晋升材料模板主键集合
     * @return 结果
     */
    public int deleteSrCharacterAscensionTemplateByIds(Long[] ids);

    /**
     * 删除角色晋升材料模板信息
     * 
     * @param id 角色晋升材料模板主键
     * @return 结果
     */
    public int deleteSrCharacterAscensionTemplateById(Long id);

    /**
     * 批量插入角色晋升材料模板（如创建晋升方案时）
     * 
     * @param templates 模板列表
     * @return 插入数量
     */
    public int insertBatch(List<SrCharacterAscensionTemplate> templates);

    /**
     * 替换式插入（先删后插）
     * 同一 templateType 视为同一模板，已有则覆盖，无有则新增
     *
     * @param templates 模板列表（必须同一 templateType）
     * @return 插入数量
     */
    public int replaceByTemplateType(List<SrCharacterAscensionTemplate> templates);

    /**
     * 根据模板类型查询完整模板数据（用于创建页面回显）
     *
     * @param templateType 模板类型
     * @return 该类型的模板记录列表
     */
    public List<SrCharacterAscensionTemplate> selectByTemplateType(String templateType);
}
