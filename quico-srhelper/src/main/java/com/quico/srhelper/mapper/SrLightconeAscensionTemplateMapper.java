package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrLightconeAscensionTemplate;

/**
 * 光锥晋升素材模板Mapper接口
 * 
 * @author quico
 * @date 2026-06-20
 */
public interface SrLightconeAscensionTemplateMapper 
{
    /**
     * 查询光锥晋升素材模板
     * 
     * @param id 光锥晋升素材模板主键
     * @return 光锥晋升素材模板
     */
    public SrLightconeAscensionTemplate selectSrLightconeAscensionTemplateById(Long id);

    /**
     * 查询光锥晋升素材模板列表
     * 
     * @param srLightconeAscensionTemplate 光锥晋升素材模板
     * @return 光锥晋升素材模板集合
     */
    public List<SrLightconeAscensionTemplate> selectSrLightconeAscensionTemplateList(SrLightconeAscensionTemplate srLightconeAscensionTemplate);

    /**
     * 新增光锥晋升素材模板
     * 
     * @param srLightconeAscensionTemplate 光锥晋升素材模板
     * @return 结果
     */
    public int insertSrLightconeAscensionTemplate(SrLightconeAscensionTemplate srLightconeAscensionTemplate);

    /**
     * 修改光锥晋升素材模板
     * 
     * @param srLightconeAscensionTemplate 光锥晋升素材模板
     * @return 结果
     */
    public int updateSrLightconeAscensionTemplate(SrLightconeAscensionTemplate srLightconeAscensionTemplate);

    /**
     * 删除光锥晋升素材模板
     * 
     * @param id 光锥晋升素材模板主键
     * @return 结果
     */
    public int deleteSrLightconeAscensionTemplateById(Long id);

    /**
     * 批量删除光锥晋升素材模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrLightconeAscensionTemplateByIds(Long[] ids);

    /**
     * 批量插入光锥晋升素材模板
     */
    public int insertBatch(List<SrLightconeAscensionTemplate> templates);

    /**
     * 根据模板类型删除所有记录
     */
    public int deleteByTemplateType(String templateType);
}
