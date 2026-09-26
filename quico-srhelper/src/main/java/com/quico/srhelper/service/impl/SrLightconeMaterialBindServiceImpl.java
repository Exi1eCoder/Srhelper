package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import com.quico.srhelper.service.ISrLightconeMaterialBindService;

/**
 * 光锥材料绑定Service业务层处理
 * 
 * @author quico
 * @date 2026-08-14
 */
@Service
public class SrLightconeMaterialBindServiceImpl implements ISrLightconeMaterialBindService 
{
    @Autowired
    private SrLightconeMaterialBindMapper srLightconeMaterialBindMapper;

    /**
     * 查询光锥材料绑定
     * 
     * @param id 光锥材料绑定主键
     * @return 光锥材料绑定
     */
    @Override
    public SrLightconeMaterialBind selectSrLightconeMaterialBindById(Long id)
    {
        return srLightconeMaterialBindMapper.selectSrLightconeMaterialBindById(id);
    }

    /**
     * 查询光锥材料绑定列表
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 光锥材料绑定
     */
    @Override
    public List<SrLightconeMaterialBind> selectSrLightconeMaterialBindList(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        return srLightconeMaterialBindMapper.selectSrLightconeMaterialBindList(srLightconeMaterialBind);
    }

    /**
     * 新增光锥材料绑定
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    @Override
    public int insertSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        srLightconeMaterialBind.setCreateTime(DateUtils.getNowDate());
        return srLightconeMaterialBindMapper.insertSrLightconeMaterialBind(srLightconeMaterialBind);
    }

    /**
     * 修改光锥材料绑定
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    @Override
    public int updateSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        srLightconeMaterialBind.setUpdateTime(DateUtils.getNowDate());
        return srLightconeMaterialBindMapper.updateSrLightconeMaterialBind(srLightconeMaterialBind);
    }

    /**
     * 批量删除光锥材料绑定
     * 
     * @param ids 需要删除的光锥材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrLightconeMaterialBindByIds(Long[] ids)
    {
        return srLightconeMaterialBindMapper.deleteSrLightconeMaterialBindByIds(ids);
    }

    /**
     * 删除光锥材料绑定信息
     * 
     * @param id 光锥材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrLightconeMaterialBindById(Long id)
    {
        return srLightconeMaterialBindMapper.deleteSrLightconeMaterialBindById(id);
    }
}
