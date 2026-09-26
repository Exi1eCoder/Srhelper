package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.SrCharacterAscensionTemplate;

/**
 * 角色材料服务接口
 */
public interface ISrCharacterMaterialService {
    
    /**
     * 绑定角色材料（四种核心素材）
     * 
     * @param characterId 角色ID
     * @param binds 材料绑定列表（最多4个，分别对应TRA/CAL/SS/EOW）
     * @return 绑定结果
     */
    void bindCharacterMaterials(Long characterId, List<SrCharacterMaterialBind> binds);
    
    /**
     * 根据角色ID获取绑定的材料列表
     * 
     * @param characterId 角色ID
     * @return 材料绑定列表
     */
    List<SrCharacterMaterialBind> getCharacterMaterials(Long characterId);
    
    /**
     * 根据角色ID和材料类型获取绑定的材料
     * 
     * @param characterId 角色ID
     * @param itemAscensionType 材料类型
     * @return 材料绑定
     */
    SrCharacterMaterialBind getCharacterMaterial(Long characterId, String itemAscensionType);
    
    /**
     * 删除角色材料绑定
     * 
     * @param characterId 角色ID
     */
    void deleteCharacterMaterials(Long characterId);
    
    /**
     * 根据角色绑定的材料和模板，自动生成晋升材料数据
     * 
     * @param characterId 角色ID
     */
    void generateAscensionMaterials(Long characterId);
    
    /**
     * 获取所有角色晋升材料模板
     * 
     * @return 模板列表
     */
    List<SrCharacterAscensionTemplate> getAllTemplates();
    
    /**
     * 根据角色模板类型获取模板
     * 
     * @param templateType 角色模板类型
     * @return 模板列表
     */
    List<SrCharacterAscensionTemplate> getTemplatesByType(String templateType);
    
    /**
     * 保存角色晋升材料模板
     * 
     * @param template 模板
     * @return 结果
     */
    int saveTemplate(SrCharacterAscensionTemplate template);
    
    /**
     * 批量保存角色晋升材料模板
     * 
     * @param templates 模板列表
     * @return 结果
     */
    int saveTemplates(List<SrCharacterAscensionTemplate> templates);
    
    /**
     * 删除角色晋升材料模板
     * 
     * @param id 模板ID
     * @return 结果
     */
    int deleteTemplate(Long id);
    
    /**
     * 根据角色命途获取可用的拟造花萼材料列表
     * 
     * @param path 角色命途
     * @return 材料列表
     */
    List<com.quico.srhelper.domain.SrItem> getAvailableCalMaterials(String path);
    
    /**
     * 根据角色属性获取可用的凝滞虚影材料列表
     * 
     * @param combatType 角色属性
     * @return 材料列表
     */
    List<com.quico.srhelper.domain.SrItem> getAvailableSsMaterials(String combatType);
    
    /**
     * 获取所有世界掉落材料列表
     * 
     * @return 材料列表
     */
    List<com.quico.srhelper.domain.SrItem> getAllTraMaterials();
    
    /**
     * 获取所有历战余响材料列表
     * 
     * @return 材料列表
     */
    List<com.quico.srhelper.domain.SrItem> getAllEowMaterials();
}