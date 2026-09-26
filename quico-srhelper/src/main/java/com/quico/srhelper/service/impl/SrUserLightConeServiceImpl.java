package com.quico.srhelper.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrUserLightConeMapper;
import com.quico.srhelper.domain.SrUserLightCone;
import com.quico.srhelper.service.ISrUserLightConeService;

/**
 * 用户持有光锥Service实现类
 * 
 * @author quico
 * @date 2026-06-07
 */
@Service
public class SrUserLightConeServiceImpl implements ISrUserLightConeService 
{
    @Autowired
    private SrUserLightConeMapper srUserLightConeMapper;

    /**
     * 查询用户持有光锥
     * 
     * @param id 用户持有光锥主键
     * @return 用户持有光锥
     */
    @Override
    public SrUserLightCone selectSrUserLightConeById(Long id)
    {
        return srUserLightConeMapper.selectSrUserLightConeById(id);
    }

    /**
     * 查询用户持有光锥列表
     * 
     * @param srUserLightCone 用户持有光锥
     * @return 用户持有光锥
     */
    @Override
    public List<SrUserLightCone> selectSrUserLightConeList(SrUserLightCone srUserLightCone)
    {
        return srUserLightConeMapper.selectSrUserLightConeList(srUserLightCone);
    }

    /**
     * 根据用户ID查询持有光锥列表（包含光锥详情）
     * 
     * @param userId 用户ID
     * @return 用户持有光锥集合（包含光锥名称、图片等）
     */
    @Override
    public List<SrUserLightCone> selectByUserIdWithDetails(Long userId)
    {
        List<SrUserLightCone> list = srUserLightConeMapper.selectByUserIdWithDetails(userId);
        return list;
    }

    @Override
    public SrUserLightCone selectByIdWithDetails(Long id)
    {
        return srUserLightConeMapper.selectByIdWithDetails(id);
    }

    /**
     * 新增用户持有光锥
     * 
     * @param srUserLightCone 用户持有光锥
     * @return 结果
     */
    @Override
    public int insertSrUserLightCone(SrUserLightCone srUserLightCone)
    {
        return srUserLightConeMapper.insertSrUserLightCone(srUserLightCone);
    }

    /**
     * 修改用户持有光锥
     * 
     * @param srUserLightCone 用户持有光锥
     * @return 结果
     */
    @Override
    public int updateSrUserLightCone(SrUserLightCone srUserLightCone)
    {
        return srUserLightConeMapper.updateSrUserLightCone(srUserLightCone);
    }

    /**
     * 批量删除用户持有光锥
     * 
     * @param ids 需要删除的用户持有光锥主键集合
     * @return 结果
     */
    @Override
    public int deleteSrUserLightConeByIds(Long[] ids)
    {
        return srUserLightConeMapper.deleteSrUserLightConeByIds(ids);
    }

    /**
     * 删除用户持有光锥信息
     * 
     * @param id 用户持有光锥主键
     * @return 结果
     */
    @Override
    public int deleteSrUserLightConeById(Long id)
    {
        return srUserLightConeMapper.deleteSrUserLightConeById(id);
    }
}