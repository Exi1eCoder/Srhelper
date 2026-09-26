package com.quico.srhelper.service.impl;

import java.util.Date;
import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.mapper.SrLightconeAscensionTemplateMapper;
import com.quico.srhelper.domain.SrLightconeAscensionTemplate;
import com.quico.srhelper.service.ISrLightconeAscensionTemplateService;

/**
 * 光锥晋升素材模板Service业务层处理
 * 
 * @author quico
 * @date 2026-06-20
 */
@Service
public class SrLightconeAscensionTemplateServiceImpl implements ISrLightconeAscensionTemplateService 
{
    @Autowired
    private SrLightconeAscensionTemplateMapper srLightconeAscensionTemplateMapper;

    /**
     * 查询光锥晋升素材模板
     * 
     * @param id 光锥晋升素材模板主键
     * @return 光锥晋升素材模板
     */
    @Override
    public SrLightconeAscensionTemplate selectSrLightconeAscensionTemplateById(Long id)
    {
        return srLightconeAscensionTemplateMapper.selectSrLightconeAscensionTemplateById(id);
    }

    /**
     * 查询光锥晋升素材模板列表
     * 
     * @param srLightconeAscensionTemplate 光锥晋升素材模板
     * @return 光锥晋升素材模板
     */
    @Override
    public List<SrLightconeAscensionTemplate> selectSrLightconeAscensionTemplateList(SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        return srLightconeAscensionTemplateMapper.selectSrLightconeAscensionTemplateList(srLightconeAscensionTemplate);
    }

    /**
     * 新增光锥晋升素材模板
     * 
     * @param srLightconeAscensionTemplate 光锥晋升素材模板
     * @return 结果
     */
    @Override
    public int insertSrLightconeAscensionTemplate(SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        srLightconeAscensionTemplate.setCreateTime(DateUtils.getNowDate());
        return srLightconeAscensionTemplateMapper.insertSrLightconeAscensionTemplate(srLightconeAscensionTemplate);
    }

    /**
     * 修改光锥晋升素材模板
     * 
     * @param srLightconeAscensionTemplate 光锥晋升素材模板
     * @return 结果
     */
    @Override
    public int updateSrLightconeAscensionTemplate(SrLightconeAscensionTemplate srLightconeAscensionTemplate)
    {
        srLightconeAscensionTemplate.setUpdateTime(DateUtils.getNowDate());
        return srLightconeAscensionTemplateMapper.updateSrLightconeAscensionTemplate(srLightconeAscensionTemplate);
    }

    /**
     * 批量删除光锥晋升素材模板
     * 
     * @param ids 需要删除的光锥晋升素材模板主键
     * @return 结果
     */
    @Override
    public int deleteSrLightconeAscensionTemplateByIds(Long[] ids)
    {
        return srLightconeAscensionTemplateMapper.deleteSrLightconeAscensionTemplateByIds(ids);
    }

    /**
     * 删除光锥晋升素材模板信息
     * 
     * @param id 光锥晋升素材模板主键
     * @return 结果
     */
    @Override
    public int deleteSrLightconeAscensionTemplateById(Long id)
    {
        return srLightconeAscensionTemplateMapper.deleteSrLightconeAscensionTemplateById(id);
    }

    /**
     * 替换式批量插入（先删后插）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceByTemplateType(List<SrLightconeAscensionTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            throw new IllegalArgumentException("模板列表不能为空");
        }
        String templateType = templates.get(0).getTemplateType();
        if (templateType == null || templateType.isEmpty()) {
            throw new IllegalArgumentException("模板类型不能为空");
        }
        // 删除旧数据
        srLightconeAscensionTemplateMapper.deleteByTemplateType(templateType);
        // 设置创建时间并批量插入
        Date now = DateUtils.getNowDate();
        for (SrLightconeAscensionTemplate t : templates) {
            t.setCreateTime(now);
        }
        return srLightconeAscensionTemplateMapper.insertBatch(templates);
    }
}
