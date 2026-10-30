package com.quico.srhelper.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.quico.srhelper.constants.SrhelperCacheConstants;
import com.quico.srhelper.domain.cache.SrReleaseVersionListCache;
import com.quico.srhelper.mapper.SrReleaseVersionMapper;
import com.quico.srhelper.domain.SrReleaseVersion;
import com.quico.srhelper.service.ISrReleaseVersionService;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏版本信息Service业务层处理
 * 
 * @author quico
 * @date 2026-10-29
 */
@Service
@Slf4j
public class SrReleaseVersionServiceImpl implements ISrReleaseVersionService 
{
    @Autowired
    private SrReleaseVersionMapper srReleaseVersionMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /** 版本列表默认查询缓存 key */
    private static final String LIST_CACHE_KEY = SrhelperCacheConstants.RELEASE_VERSION_LIST_KEY + "list";

    /**
     * 查询游戏版本信息
     * 
     * @param id 游戏版本信息主键
     * @return 游戏版本信息
     */
    @Override
    public SrReleaseVersion selectSrReleaseVersionById(Long id)
    {
        return srReleaseVersionMapper.selectSrReleaseVersionById(id);
    }

    /**
     * 查询游戏版本信息列表
     * 无过滤条件时走 Redis 缓存，有过滤条件时直接查库
     * 
     * @param srReleaseVersion 游戏版本信息
     * @return 游戏版本信息
     */
    @Override
    public List<SrReleaseVersion> selectSrReleaseVersionList(SrReleaseVersion srReleaseVersion)
    {
        if (isEmptyQuery(srReleaseVersion))
        {
            SrReleaseVersionListCache cached = (SrReleaseVersionListCache) redisTemplate.opsForValue().get(LIST_CACHE_KEY);
            if (cached != null)
            {
                log.debug("selectSrReleaseVersionList 命中缓存");
                return cached.getList();
            }
            List<SrReleaseVersion> list = srReleaseVersionMapper.selectSrReleaseVersionList(srReleaseVersion);
            redisTemplate.opsForValue().set(LIST_CACHE_KEY, new SrReleaseVersionListCache(list),
                    SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
            return list;
        }
        return srReleaseVersionMapper.selectSrReleaseVersionList(srReleaseVersion);
    }

    /**
     * 新增游戏版本信息
     * 
     * @param srReleaseVersion 游戏版本信息
     * @return 结果
     */
    @Override
    public int insertSrReleaseVersion(SrReleaseVersion srReleaseVersion)
    {
        srReleaseVersion.setCreateTime(DateUtils.getNowDate());
        srReleaseVersion.setMajorVersion(srReleaseVersion.getVersionCode().split("\\.")[0]+".0");
        int result = srReleaseVersionMapper.insertSrReleaseVersion(srReleaseVersion);
        clearListCache();
        return result;
    }

    /**
     * 修改游戏版本信息
     * 
     * @param srReleaseVersion 游戏版本信息
     * @return 结果
     */
    @Override
    public int updateSrReleaseVersion(SrReleaseVersion srReleaseVersion)
    {
        srReleaseVersion.setUpdateTime(DateUtils.getNowDate());
        int result = srReleaseVersionMapper.updateSrReleaseVersion(srReleaseVersion);
        clearListCache();
        return result;
    }

    /**
     * 批量删除游戏版本信息
     * 
     * @param ids 需要删除的游戏版本信息主键
     * @return 结果
     */
    @Override
    public int deleteSrReleaseVersionByIds(Long[] ids)
    {
        int result = srReleaseVersionMapper.deleteSrReleaseVersionByIds(ids);
        clearListCache();
        return result;
    }

    /**
     * 删除游戏版本信息信息
     * 
     * @param id 游戏版本信息主键
     * @return 结果
     */
    @Override
    public int deleteSrReleaseVersionById(Long id)
    {
        int result = srReleaseVersionMapper.deleteSrReleaseVersionById(id);
        clearListCache();
        return result;
    }

    /**
     * 清除版本列表缓存
     */
    private void clearListCache()
    {
        redisTemplate.delete(LIST_CACHE_KEY);
    }

    /**
     * 判断是否为无过滤条件的列表查询（当前仅 versionCode 参与筛选）
     */
    private boolean isEmptyQuery(SrReleaseVersion srReleaseVersion)
    {
        return srReleaseVersion == null || StringUtils.isEmpty(srReleaseVersion.getVersionCode());
    }
}
