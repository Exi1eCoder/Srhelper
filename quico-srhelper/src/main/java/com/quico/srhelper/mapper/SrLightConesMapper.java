package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrLightCones;
import org.apache.ibatis.annotations.Param;

/**
 * 光锥一览Mapper接口
 * 
 * @author quico
 * @date 2026-05-25
 */
public interface SrLightConesMapper 
{
    /**
     * 查询光锥一览
     * 
     * @param id 光锥一览主键
     * @return 光锥一览
     */
    public SrLightCones selectSrLightConesById(Long id);

    /**
     * 按光锥名称 + 实装版本精确查询（导入去重用，避免 selectSrLightConesList 的 like 模糊查询）
     *
     * @param lightConeName  光锥名称
     * @param releaseVersion 实装版本（可为 null，匹配 IS NULL）
     * @return 光锥一览
     */
    public SrLightCones selectSrLightConesByNameAndVersion(@Param("lightConeName") String lightConeName,
    @Param("releaseVersion") String releaseVersion);

    /**
     * 按光锥名称精确查询全部记录（材料绑定导入用：同名多版本时返回多条，由业务层判定）
     *
     * @param lightConeName 光锥名称
     * @return 光锥列表
     */
    public List<SrLightCones> selectSrLightConesListByName(@Param("lightConeName") String lightConeName);

    /**
     * 查询光锥一览列表
     * 
     * @param srLightCones 光锥一览
     * @return 光锥一览集合
     */
    public List<SrLightCones> selectSrLightConesList(SrLightCones srLightCones);

    /**
     * 新增光锥一览
     * 
     * @param srLightCones 光锥一览
     * @return 结果
     */
    public int insertSrLightCones(SrLightCones srLightCones);

    /**
     * 修改光锥一览
     * 
     * @param srLightCones 光锥一览
     * @return 结果
     */
    public int updateSrLightCones(SrLightCones srLightCones);

    /**
     * 删除光锥一览
     * 
     * @param id 光锥一览主键
     * @return 结果
     */
    public int deleteSrLightConesById(Long id);

    /**
     * 批量删除光锥一览
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrLightConesByIds(Long[] ids);
}
