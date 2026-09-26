package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrLightConeAscension;

/**
 * 光锥晋升材料Mapper接口
 *
 * @author quico
 * @date 2026-05-30
 */
public interface SrLightConeAscensionMapper
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
     * 删除光锥晋升材料
     *
     * @param id 光锥晋升材料主键
     * @return 结果
     */
    public int deleteSrLightConeAscensionById(Long id);

    /**
     * 批量删除光锥晋升材料
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrLightConeAscensionByIds(Long[] ids);

    /**
     * 根据光锥ID删除所有晋升材料
     *
     * @param lightConeId 光锥ID
     * @return 结果
     */
    public int deleteSrLightConeAscensionByConeId(Long lightConeId);

    /**
     * 批量插入晋升材料
     */
    public int insertBatch(@org.apache.ibatis.annotations.Param("list") List<SrLightConeAscension> list);
}
