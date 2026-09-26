package com.quico.infra.mapper;

import java.util.List;
import com.quico.infra.domain.SrMaterial;
import com.quico.infra.domain.SrMaterialGroup;

/**
 * 素材库Mapper接口
 * 
 * @author quico
 * @date 2026-05-23
 */
public interface SrMaterialMapper 
{
    /**
     * 查询素材库
     * 
     * @param id 素材库主键
     * @return 素材库
     */
    public SrMaterial selectSrMaterialById(Long id);

    /**
     * 查询素材库列表
     * 
     * @param srMaterial 素材库
     * @return 素材库集合
     */
    public List<SrMaterial> selectSrMaterialList(SrMaterial srMaterial);

    /**
     * 新增素材库
     * 
     * @param srMaterial 素材库
     * @return 结果
     */
    public int insertSrMaterial(SrMaterial srMaterial);

    /**
     * 修改素材库
     * 
     * @param srMaterial 素材库
     * @return 结果
     */
    public int updateSrMaterial(SrMaterial srMaterial);

    /**
     * 删除素材库
     * 
     * @param id 素材库主键
     * @return 结果
     */
    public int deleteSrMaterialById(Long id);

    /**
     * 批量删除素材库
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrMaterialByIds(Long[] ids);

    /**
     * 批量删除素材分组
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrMaterialGroupByIds(Long[] ids);
    
    /**
     * 批量新增素材分组
     * 
     * @param srMaterialGroupList 素材分组列表
     * @return 结果
     */
    public int batchSrMaterialGroup(List<SrMaterialGroup> srMaterialGroupList);
    

    /**
     * 通过素材库主键删除素材分组信息
     * 
     * @param id 素材库ID
     * @return 结果
     */
    public int deleteSrMaterialGroupById(Long id);

    /**
     * 批量查询素材分组
     *
     * @param ids 分组ID数组
     * @return 素材分组列表
     */
    public List<SrMaterialGroup> selectSrMaterialGroupByIds(Long[] ids);
}
