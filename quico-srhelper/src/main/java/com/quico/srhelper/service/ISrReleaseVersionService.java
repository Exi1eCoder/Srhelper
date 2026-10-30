package com.quico.srhelper.service;

import java.util.List;
import com.quico.srhelper.domain.SrReleaseVersion;

/**
 * 游戏版本信息Service接口
 * 
 * @author quico
 * @date 2026-10-29
 */
public interface ISrReleaseVersionService 
{
    /**
     * 查询游戏版本信息
     * 
     * @param id 游戏版本信息主键
     * @return 游戏版本信息
     */
    public SrReleaseVersion selectSrReleaseVersionById(Long id);

    /**
     * 查询游戏版本信息列表
     * 
     * @param srReleaseVersion 游戏版本信息
     * @return 游戏版本信息集合
     */
    public List<SrReleaseVersion> selectSrReleaseVersionList(SrReleaseVersion srReleaseVersion);

    /**
     * 新增游戏版本信息
     * 
     * @param srReleaseVersion 游戏版本信息
     * @return 结果
     */
    public int insertSrReleaseVersion(SrReleaseVersion srReleaseVersion);

    /**
     * 修改游戏版本信息
     * 
     * @param srReleaseVersion 游戏版本信息
     * @return 结果
     */
    public int updateSrReleaseVersion(SrReleaseVersion srReleaseVersion);

    /**
     * 批量删除游戏版本信息
     * 
     * @param ids 需要删除的游戏版本信息主键集合
     * @return 结果
     */
    public int deleteSrReleaseVersionByIds(Long[] ids);

    /**
     * 删除游戏版本信息信息
     * 
     * @param id 游戏版本信息主键
     * @return 结果
     */
    public int deleteSrReleaseVersionById(Long id);
}
