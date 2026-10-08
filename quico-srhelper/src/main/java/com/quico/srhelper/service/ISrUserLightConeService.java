package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrUserLightCone;

/**
 * 用户持有光锥Service接口
 * 
 * @author quico
 * @date 2026-06-07
 */
public interface ISrUserLightConeService 
{
    /**
     * 查询用户持有光锥
     * 
     * @param id 用户持有光锥主键
     * @return 用户持有光锥
     */
    public SrUserLightCone selectSrUserLightConeById(Long id);

    /**
     * 查询用户持有光锥列表
     * 
     * @param srUserLightCone 用户持有光锥
     * @return 用户持有光锥集合
     */
    public List<SrUserLightCone> selectSrUserLightConeList(SrUserLightCone srUserLightCone);

    /**
     * 根据用户ID查询持有光锥列表（包含光锥详情）
     *
     * @param userId 用户ID
     * @param path 命途过滤（可选）
     * @param starLevel 星级过滤（可选）
     * @param lightConeName 光锥名称模糊搜索（可选）
     * @return 用户持有光锥集合（包含光锥名称、图片等）
     */
    public List<SrUserLightCone> selectByUserIdWithDetails(Long userId, String path, Integer starLevel, String lightConeName);

    /**
     * 根据ID查询持有光锥（包含光锥详情）
     *
     * @param id 主键
     * @return 用户持有光锥（包含光锥名称、图片、星级、命途）
     */
    public SrUserLightCone selectByIdWithDetails(Long id);

    /**
     * 新增用户持有光锥
     * 
     * @param srUserLightCone 用户持有光锥
     * @return 结果
     */
    public int insertSrUserLightCone(SrUserLightCone srUserLightCone);

    /**
     * 修改用户持有光锥
     * 
     * @param srUserLightCone 用户持有光锥
     * @return 结果
     */
    public int updateSrUserLightCone(SrUserLightCone srUserLightCone);

    /**
     * 批量删除用户持有光锥
     * 
     * @param ids 需要删除的用户持有光锥主键集合
     * @return 结果
     */
    public int deleteSrUserLightConeByIds(Long[] ids);

    /**
     * 删除用户持有光锥信息
     * 
     * @param id 用户持有光锥主键
     * @return 结果
     */
    public int deleteSrUserLightConeById(Long id);
}