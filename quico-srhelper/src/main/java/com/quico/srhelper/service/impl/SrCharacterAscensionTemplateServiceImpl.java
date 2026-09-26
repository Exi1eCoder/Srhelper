package com.quico.srhelper.service.impl;

import java.util.List;
import java.util.Date;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.mapper.SrCharacterAscensionTemplateMapper;
import com.quico.srhelper.domain.SrCharacterAscensionTemplate;
import com.quico.srhelper.service.ISrCharacterAscensionTemplateService;

/**
 * 角色晋升材料模板Service业务层处理
 * 
 * @author quico
 * @date 2026-06-15
 */
@Service
public class SrCharacterAscensionTemplateServiceImpl implements ISrCharacterAscensionTemplateService 
{
    @Autowired
    private SrCharacterAscensionTemplateMapper srCharacterAscensionTemplateMapper;

    /**
     * 查询角色晋升材料模板
     * 
     * @param id 角色晋升材料模板主键
     * @return 角色晋升材料模板
     */
    @Override
    public SrCharacterAscensionTemplate selectSrCharacterAscensionTemplateById(Long id)
    {
        return srCharacterAscensionTemplateMapper.selectSrCharacterAscensionTemplateById(id);
    }

    /**
     * 查询角色晋升材料模板列表
     * 
     * @param srCharacterAscensionTemplate 角色晋升材料模板
     * @return 角色晋升材料模板
     */
    @Override
    public List<SrCharacterAscensionTemplate> selectSrCharacterAscensionTemplateList(SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        return srCharacterAscensionTemplateMapper.selectSrCharacterAscensionTemplateList(srCharacterAscensionTemplate);
    }

    /**
     * 新增角色晋升材料模板
     * 
     * @param srCharacterAscensionTemplate 角色晋升材料模板
     * @return 结果
     */
    @Override
    public int insertSrCharacterAscensionTemplate(SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        srCharacterAscensionTemplate.setCreateTime(DateUtils.getNowDate());
        return srCharacterAscensionTemplateMapper.insertSrCharacterAscensionTemplate(srCharacterAscensionTemplate);
    }

    /**
     * 修改角色晋升材料模板
     * 
     * @param srCharacterAscensionTemplate 角色晋升材料模板
     * @return 结果
     */
    @Override
    public int updateSrCharacterAscensionTemplate(SrCharacterAscensionTemplate srCharacterAscensionTemplate)
    {
        srCharacterAscensionTemplate.setUpdateTime(DateUtils.getNowDate());
        return srCharacterAscensionTemplateMapper.updateSrCharacterAscensionTemplate(srCharacterAscensionTemplate);
    }

    /**
     * 批量删除角色晋升材料模板
     * 
     * @param ids 需要删除的角色晋升材料模板主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterAscensionTemplateByIds(Long[] ids)
    {
        return srCharacterAscensionTemplateMapper.deleteSrCharacterAscensionTemplateByIds(ids);
    }

    /**
     * 删除角色晋升材料模板信息
     * 
     * @param id 角色晋升材料模板主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterAscensionTemplateById(Long id)
    {
        return srCharacterAscensionTemplateMapper.deleteSrCharacterAscensionTemplateById(id);
    }

    /**
     * 批量插入角色晋升材料模板（如创建晋升方案时）
     * 统一设置创建时间，支持事务回滚
     * 
     * @param templates 模板列表
     * @return 插入数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatch(List<SrCharacterAscensionTemplate> templates)
    {
        Date now = DateUtils.getNowDate();
        for (SrCharacterAscensionTemplate t : templates) {
            t.setCreateTime(now);
        }
        return srCharacterAscensionTemplateMapper.insertBatch(templates);
    }

    /**
     * 替换式插入（先删后插）
     * 传入的同一批次的 templateType 视为同一模板：
     * - 若该 templateType 已有数据 → 先删除旧数据，再插入新数据
     * - 若该 templateType 无数据 → 直接插入
     * 整个操作在一个事务中完成，保证原子性
     *
     * @param templates 模板列表（必须同一 templateType）
     * @return 插入数量
     * @throws IllegalArgumentException 如果列表为空或模板类型不一致
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceByTemplateType(List<SrCharacterAscensionTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            throw new IllegalArgumentException("模板列表不能为空");
        }
        String templateType = templates.get(0).getTemplateType();
        if (templateType == null || templateType.isEmpty()) {
            throw new IllegalArgumentException("模板类型不能为空");
        }
        // 校验同一批次 templateType 一致（防御性）
        for (SrCharacterAscensionTemplate t : templates) {
            if (!templateType.equals(t.getTemplateType())) {
                throw new IllegalArgumentException("同一批次不允许混入不同的 templateType: "
                    + templateType + " vs " + t.getTemplateType());
            }
        }
        // 1. 删除该 templateType 下的所有旧数据
        srCharacterAscensionTemplateMapper.deleteByTemplateType(templateType);

        // 2. 设置创建时间，批量插入新数据
        Date now = DateUtils.getNowDate();
        for (SrCharacterAscensionTemplate t : templates) {
            t.setCreateTime(now);
        }
        int inserted = srCharacterAscensionTemplateMapper.insertBatch(templates);

        return inserted;
    }

    /**
     * 根据模板类型查询完整模板数据（用于创建页面回显）
     *
     * @param templateType 模板类型
     * @return 该类型的模板记录列表
     */
    @Override
    public List<SrCharacterAscensionTemplate> selectByTemplateType(String templateType) {
        return srCharacterAscensionTemplateMapper.selectByTemplateType(templateType);
    }
}
