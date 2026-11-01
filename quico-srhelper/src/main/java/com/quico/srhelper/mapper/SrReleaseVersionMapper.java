package com.quico.srhelper.mapper;

import java.util.List;
import com.quico.srhelper.domain.SrReleaseVersion;
import org.apache.ibatis.annotations.Param;

/**
 * 游戏版本信息Mapper接口
 * 
 * @author quico
 * @date 2026-10-29
 */
public interface SrReleaseVersionMapper 
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
     * 按版本号精确查询（版本联动：取实装时间）
     *
     * @param versionCode 版本号（如 3.2）
     * @return 版本信息，无匹配返回 null
     */
    public SrReleaseVersion selectSrReleaseVersionByVersionCode(@Param("versionCode") String versionCode);

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
     * 删除游戏版本信息
     * 
     * @param id 游戏版本信息主键
     * @return 结果
     */
    public int deleteSrReleaseVersionById(Long id);

    /**
     * 批量删除游戏版本信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSrReleaseVersionByIds(Long[] ids);
}
