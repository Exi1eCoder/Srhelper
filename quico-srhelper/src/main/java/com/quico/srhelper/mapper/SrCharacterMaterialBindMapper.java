package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrCharacterMaterialBind;

/**
 * 角色材料绑定Mapper接口
 * 
 * @author quico
 * @date 2026-06-14
 */
public interface SrCharacterMaterialBindMapper 
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
     * 删除角色材料绑定
     * 
     * @param id 角色材料绑定主键
     * @return 结果
     */
    public int deleteSrCharacterMaterialBindById(Long id);

    /**
     * 批量删除角色材料绑定
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrCharacterMaterialBindByIds(Long[] ids);

    /**
     * 根据角色ID删除所有绑定
     * 
     * @param characterId 角色ID
     * @return 结果
     */
    public int deleteByCharacterId(Long characterId);

    /**
     * 根据角色ID查询所有绑定
     * 
     * @param characterId 角色ID
     * @return 绑定列表
     */
    public List<SrCharacterMaterialBind> selectByCharacterId(Long characterId);

    /**
     * 更新角色名称（同步用）
     * 
     * @param characterId 角色ID
     * @param characterName 角色名称
     * @return 结果
     */
    public int updateCharacterName(Long characterId, String characterName);

    /**
     * 更新物品信息（同步用）
     * 
     * @param itemId 物品ID
     * @param itemName 物品名称
     * @param itemImage 物品图片
     * @param rarityLevel 稀有度
     * @return 结果
     */
    public int updateItemInfo(Long itemId, String itemName, String itemImage, Integer rarityLevel);

    /**
     * 更新物品系列ID（同步用）
     * 
     * @param itemId 物品ID
     * @param seriesId 系列ID
     * @return 结果
     */
    public int updateItemSeriesId(Long itemId, Long seriesId);

    /**
     * 查询所有需要同步的记录（用于定时任务）
     * 
     * @return 需要同步的记录列表
     */
    public List<SrCharacterMaterialBind> selectAllForSync();
}
