package com.quico.srhelper.service.impl;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrUserLightConeMapper;
import com.quico.srhelper.domain.SrUserLightCone;
import com.quico.srhelper.service.ISrUserLightConeService;
import com.quico.srhelper.service.helper.UsableChecker;

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

    @Autowired
    private UsableChecker usableChecker;

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
        List<SrUserLightCone> list = srUserLightConeMapper.selectSrUserLightConeList(srUserLightCone);
        markUsable(list);
        return list;
    }

    /**
     * 根据用户ID查询持有光锥列表（包含光锥详情）
     *
     * @param userId 用户ID
     * @param path 命途过滤（可选）
     * @param starLevel 星级过滤（可选）
     * @param lightConeName 光锥名称模糊搜索（可选）
     * @return 用户持有光锥集合（包含光锥名称、图片等）
     */
    @Override
    public List<SrUserLightCone> selectByUserIdWithDetails(Long userId, String path, Integer starLevel, String lightConeName)
    {
        List<SrUserLightCone> list = srUserLightConeMapper.selectByUserIdWithDetails(userId, path, starLevel, lightConeName);
        markUsable(list);
        return list;
    }

    @Override
    public SrUserLightCone selectByIdWithDetails(Long id)
    {
        SrUserLightCone lightCone = srUserLightConeMapper.selectByIdWithDetails(id);
        if (lightCone != null) {
            lightCone.setUsable(usableChecker.getUsableLightconeIds().contains(lightCone.getLightConeId()));
        }
        return lightCone;
    }

    /**
     * 按图鉴光锥材料绑定完成度标记可用性（未完成绑定的光锥不可展示与展开业务）
     */
    private void markUsable(List<SrUserLightCone> list)
    {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> usableIds = usableChecker.getUsableLightconeIds();
        for (SrUserLightCone lightCone : list) {
            lightCone.setUsable(usableIds.contains(lightCone.getLightConeId()));
        }
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