package com.quico.srhelper.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.quico.srhelper.constants.SrhelperCacheConstants;
import com.quico.srhelper.domain.cache.SrRelicSetListCache;
import com.quico.srhelper.domain.vo.SrRelicSetDetailVO;
import com.quico.srhelper.mapper.SrRelicMapper;
import com.quico.srhelper.mapper.SrRelicSetMapper;
import com.quico.srhelper.domain.SrRelicSet;
import com.quico.srhelper.service.ISrRelicSetService;
import lombok.extern.slf4j.Slf4j;

/**
 * 遗器一览Service业务层处理
 * 
 * @author quico
 * @date 2026-11-24
 */
@Service
@Slf4j
public class SrRelicSetServiceImpl implements ISrRelicSetService 
{
    @Autowired
    private SrRelicSetMapper srRelicSetMapper;

    @Autowired
    private SrRelicMapper srRelicMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /** 遗器套装列表默认查询缓存 key */
    private static final String LIST_CACHE_KEY = SrhelperCacheConstants.RELIC_SET_LIST_KEY + "list";

    /**
     * 查询遗器一览
     * 
     * @param id 遗器一览主键
     * @return 遗器一览
     */
    @Override
    public SrRelicSet selectSrRelicSetById(Long id)
    {
        return srRelicSetMapper.selectSrRelicSetById(id);
    }

    /**
     * 查询遗器套装详情（含套装下的遗器部件）
     * 
     * @param id 遗器一览主键
     * @return 遗器套装详情
     */
    @Override
    public SrRelicSetDetailVO selectSrRelicSetDetail(Long id)
    {
        SrRelicSet relicSet = srRelicSetMapper.selectSrRelicSetById(id);
        if (relicSet == null)
        {
            return null;
        }
        SrRelicSetDetailVO vo = new SrRelicSetDetailVO();
        BeanUtils.copyProperties(relicSet, vo);
        vo.setRelics(srRelicMapper.selectRelicPiecesBySetId(relicSet.getSetId()));
        return vo;
    }

    /**
     * 查询遗器一览列表
     * 无过滤条件时走 Redis 缓存，有过滤条件时直接查库
     * 
     * @param srRelicSet 遗器一览
     * @return 遗器一览
     */
    @Override
    public List<SrRelicSet> selectSrRelicSetList(SrRelicSet srRelicSet)
    {
        if (isEmptyQuery(srRelicSet))
        {
            SrRelicSetListCache cached = (SrRelicSetListCache) redisTemplate.opsForValue().get(LIST_CACHE_KEY);
            if (cached != null)
            {
                log.debug("selectSrRelicSetList 命中缓存");
                return cached.getList();
            }
            List<SrRelicSet> list = srRelicSetMapper.selectSrRelicSetList(srRelicSet);
            redisTemplate.opsForValue().set(LIST_CACHE_KEY, new SrRelicSetListCache(list),
                    SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
            return list;
        }
        return srRelicSetMapper.selectSrRelicSetList(srRelicSet);
    }

    /**
     * 新增遗器一览
     * 
     * @param srRelicSet 遗器一览
     * @return 结果
     */
    @Override
    public int insertSrRelicSet(SrRelicSet srRelicSet)
    {
        srRelicSet.setCreateTime(DateUtils.getNowDate());
        int result = srRelicSetMapper.insertSrRelicSet(srRelicSet);
        clearListCache();
        return result;
    }

    /**
     * 修改遗器一览
     * 
     * @param srRelicSet 遗器一览
     * @return 结果
     */
    @Override
    public int updateSrRelicSet(SrRelicSet srRelicSet)
    {
        srRelicSet.setUpdateTime(DateUtils.getNowDate());
        int result = srRelicSetMapper.updateSrRelicSet(srRelicSet);
        clearListCache();
        return result;
    }

    /**
     * 批量删除遗器一览
     * 
     * @param ids 需要删除的遗器一览主键
     * @return 结果
     */
    @Override
    public int deleteSrRelicSetByIds(Long[] ids)
    {
        int result = srRelicSetMapper.deleteSrRelicSetByIds(ids);
        clearListCache();
        return result;
    }

    /**
     * 删除遗器一览信息
     * 
     * @param id 遗器一览主键
     * @return 结果
     */
    @Override
    public int deleteSrRelicSetById(Long id)
    {
        int result = srRelicSetMapper.deleteSrRelicSetById(id);
        clearListCache();
        return result;
    }

    /**
     * 清除遗器套装列表缓存
     */
    private void clearListCache()
    {
        redisTemplate.delete(LIST_CACHE_KEY);
    }

    /**
     * 判断是否为无过滤条件的列表查询（当前仅 setName、setCategory、releaseVersion 参与筛选）
     */
    private boolean isEmptyQuery(SrRelicSet srRelicSet)
    {
        return srRelicSet == null
                || (StringUtils.isEmpty(srRelicSet.getSetName())
                && StringUtils.isEmpty(srRelicSet.getSetCategory())
                && StringUtils.isEmpty(srRelicSet.getReleaseVersion()));
    }
}
