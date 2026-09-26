package com.quico.infra.service;

import java.util.List;
import com.quico.infra.domain.SrMaterial;
import com.quico.infra.domain.vo.SrMaterialVO;

/**
 * 素材库Service接口
 * 
 * @author quico
 * @date 2026-05-23
 */
public interface ISrMaterialService 
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
     * 批量删除素材库
     * 
     * @param ids 需要删除的素材库主键集合
     * @return 结果
     */
    public int deleteSrMaterialByIds(Long[] ids);

    /**
     * 删除素材库信息
     * 
     * @param id 素材库主键
     * @return 结果
     */
    public int deleteSrMaterialById(Long id);

    /**
     * 组装素材库VO列表（批量查用户和分组）
     *
     * @param materialList 素材库列表
     * @return 素材库VO列表
     */
    public List<SrMaterialVO> assembleSrMaterialVOList(List<SrMaterial> materialList);

}
