package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.dto.LightConeAscensionBatchSaveDTO;

/**
 * 光锥晋升材料Service接口
 *
 * @author quico
 * @date 2026-05-30
 */
public interface ISrLightConeAscensionService
{
    /**
     * 查询光锥晋升材料
     *
     * @param id 光锥晋升材料主键
     * @return 光锥晋升材料
     */
    public SrLightConeAscension selectSrLightConeAscensionById(Long id);

    /**
     * 查询光锥晋升材料列表
     *
     * @param srLightConeAscension 光锥晋升材料
     * @return 光锥晋升材料集合
     */
    public List<SrLightConeAscension> selectSrLightConeAscensionList(SrLightConeAscension srLightConeAscension);

    /**
     * 根据光锥ID查询晋升材料列表
     *
     * @param lightConeId 光锥ID
     * @return 光锥晋升材料集合
     */
    public List<SrLightConeAscension> selectSrLightConeAscensionByConeId(Long lightConeId);

    /**
     * 新增光锥晋升材料
     *
     * @param srLightConeAscension 光锥晋升材料
     * @return 结果
     */
    public int insertSrLightConeAscension(SrLightConeAscension srLightConeAscension);

    /**
     * 修改光锥晋升材料
     *
     * @param srLightConeAscension 光锥晋升材料
     * @return 结果
     */
    public int updateSrLightConeAscension(SrLightConeAscension srLightConeAscension);

    /**
     * 批量删除光锥晋升材料
     *
     * @param ids 需要删除的光锥晋升材料主键集合
     * @return 结果
     */
    public int deleteSrLightConeAscensionByIds(Long[] ids);

    /**
     * 删除光锥晋升材料信息
     *
     * @param id 光锥晋升材料主键
     * @return 结果
     */
    public int deleteSrLightConeAscensionById(Long id);

    /**
     * 根据光锥ID删除所有晋升材料
     *
     * @param lightConeId 光锥ID
     * @return 结果
     */
    public int deleteSrLightConeAscensionByConeId(Long lightConeId);

    /**
     * 批量保存光锥晋升材料（先删后增）
     *
     * @param dto 批量保存DTO
     * @return 新插入记录的ID列表
     */
    public List<Long> batchSaveLightConeAscension(LightConeAscensionBatchSaveDTO dto);
}
