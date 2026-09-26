package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrLightconeAscensionTemplate;

/**
 * 光锥晋升素材模板Service接口
 * 
 * @author quico
 * @date 2026-06-20
 */
public interface ISrLightconeAscensionTemplateService 
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
     * 批量删除光锥晋升素材模板
     * 
     * @param ids 需要删除的光锥晋升素材模板主键集合
     * @return 结果
     */
    public int deleteSrLightconeAscensionTemplateByIds(Long[] ids);

    /**
     * 删除光锥晋升素材模板信息
     * 
     * @param id 光锥晋升素材模板主键
     * @return 结果
     */
    public int deleteSrLightconeAscensionTemplateById(Long id);

    /**
     * 替换式批量插入（先删后插）
     */
    public int replaceByTemplateType(List<SrLightconeAscensionTemplate> templates);
}
