package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterAscensionTemplate;

/**
 * 角色晋升材料模板Mapper接口
 * 
 * @author quico
 * @date 2026-06-15
 */
public interface SrCharacterAscensionTemplateMapper 
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
     * 删除角色晋升材料模板
     * 
     * @param id 角色晋升材料模板主键
     * @return 结果
     */
    public int deleteSrCharacterAscensionTemplateById(Long id);

    /**
     * 批量删除角色晋升材料模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrCharacterAscensionTemplateByIds(Long[] ids);

    /**
     * 批量插入角色晋升材料模板
     * 
     * @param templates 模板列表
     * @return 插入数量
     */
    public int insertBatch(List<SrCharacterAscensionTemplate> templates);

    /**
     * 根据模板类型删除所有记录（用于替换式更新）
     *
     * @param templateType 模板类型
     * @return 删除数量
     */
    public int deleteByTemplateType(String templateType);

    /**
     * 根据模板类型查询完整模板数据
     *
     * @param templateType 模板类型
     * @return 该类型下的所有模板记录
     */
    public List<SrCharacterAscensionTemplate> selectByTemplateType(String templateType);
}
