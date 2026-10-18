package com.quico.srhelper.service.impl;

import com.quico.srhelper.constants.SrhelperCacheConstants;
import com.quico.srhelper.domain.SrCharacterExpUpgrade;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.cache.SrCharacterExpUpgradeCache;
import com.quico.srhelper.domain.dto.SrCharacterExpUpgradeDTO;

import com.quico.srhelper.domain.vo.SrCharacterExpUpgradeVO;
import com.quico.srhelper.mapper.SrCharacterExpUpgradeMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.service.ISrCharacterExpUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色经验升级Service实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrCharacterExpUpgradeServiceImpl implements ISrCharacterExpUpgradeService {
    
    private final SrCharacterExpUpgradeMapper expUpgradeMapper;
    private final SrItemMapper srItemMapper;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String CACHE_KEY = SrhelperCacheConstants.CHAR_EXP_UPGRADE_KEY + "list";

    /** 角色经验升级展示物品名（仅用于详情/配置展示，非实际消耗材料） */
    private static final String EXP_ITEM_NAME = "经验值";
    /** 信用点物品名 */
    private static final String CREDITS_ITEM_NAME = "信用点";

    @Override
    public List<SrCharacterExpUpgradeVO> selectAllExpUpgrades() {
        SrCharacterExpUpgradeCache cached = (SrCharacterExpUpgradeCache) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            log.debug("selectAllExpUpgrades cached is not null, 从缓存中获取角色经验升级配置");
            return cached.getList();
        }
        log.debug("selectAllExpUpgrades cached is null, 从数据库中查询角色经验升级配置");
        // 经验书/信用点物品固定，整表转换前只解析一次，避免逐行重复查库
        SrItem expItem = srItemMapper.selectSrItemByName(EXP_ITEM_NAME);
        SrItem creditsItem = srItemMapper.selectSrItemByName(CREDITS_ITEM_NAME);
        List<SrCharacterExpUpgradeVO> list = expUpgradeMapper.selectAllOrderByMinLevel()
                .stream()
                .map(entity -> convertToVO(entity, expItem, creditsItem))
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(CACHE_KEY, new SrCharacterExpUpgradeCache(list), SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
        return list;
    }
    
    @Override
    public SrCharacterExpUpgradeVO selectExpUpgradeById(Long id) {
        String cacheKey = SrhelperCacheConstants.CHAR_EXP_UPGRADE_KEY + "id:" + id;
        SrCharacterExpUpgradeVO cached = (SrCharacterExpUpgradeVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("selectExpUpgradeById 命中缓存，id={}", id);
            return cached;
        }
        SrCharacterExpUpgrade entity = expUpgradeMapper.selectById(id);
        SrCharacterExpUpgradeVO vo = entity != null ? convertToVO(entity) : null;
        if (vo != null) {
            redisTemplate.opsForValue().set(cacheKey, vo, SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
        }
        return vo;
    }

    @Override
    public SrCharacterExpUpgradeVO selectExpUpgradeByLevel(Integer level) {
        String cacheKey = SrhelperCacheConstants.CHAR_EXP_UPGRADE_KEY + "level:" + level;
        SrCharacterExpUpgradeVO cached = (SrCharacterExpUpgradeVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("selectExpUpgradeByLevel 命中缓存，level={}", level);
            return cached;
        }
        SrCharacterExpUpgrade entity = expUpgradeMapper.selectByLevel(level);
        SrCharacterExpUpgradeVO vo = entity != null ? convertToVO(entity) : null;
        if (vo != null) {
            redisTemplate.opsForValue().set(cacheKey, vo, SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
        }
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertExpUpgrade(SrCharacterExpUpgradeDTO dto) {
        SrCharacterExpUpgrade entity = SrCharacterExpUpgrade.builder()
                .minLevel(dto.getMinLevel())
                .maxLevel(dto.getMaxLevel())
                .expRequired(dto.getExpRequired())
                .creditsRequired(dto.getCreditsRequired())
                .build();
        int rows = expUpgradeMapper.insert(entity);
        clearCache();
        return rows;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateExpUpgrade(SrCharacterExpUpgradeDTO dto) {
        SrCharacterExpUpgrade entity = SrCharacterExpUpgrade.builder()
                .id(dto.getId())
                .minLevel(dto.getMinLevel())
                .maxLevel(dto.getMaxLevel())
                .expRequired(dto.getExpRequired())
                .creditsRequired(dto.getCreditsRequired())
                .build();
        int rows = expUpgradeMapper.updateById(entity);
        clearCache();
        return rows;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpUpgradeById(Long id) {
        int rows = expUpgradeMapper.deleteById(id);
        clearCache();
        return rows;
    }

    private void clearCache() {
        // 删除该前缀下所有缓存（list、id:*、level:*），保证写操作后数据一致
        Set<Object> keys = redisTemplate.keys(SrhelperCacheConstants.CHAR_EXP_UPGRADE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    private SrCharacterExpUpgradeVO convertToVO(SrCharacterExpUpgrade entity) {
        return convertToVO(entity,
                srItemMapper.selectSrItemByName(EXP_ITEM_NAME),
                srItemMapper.selectSrItemByName(CREDITS_ITEM_NAME));
    }

    private SrCharacterExpUpgradeVO convertToVO(SrCharacterExpUpgrade entity, SrItem expItem, SrItem creditsItem) {
        SrCharacterExpUpgradeVO vo = new SrCharacterExpUpgradeVO();
        vo.setId(entity.getId());
        vo.setMinLevel(entity.getMinLevel());
        vo.setMaxLevel(entity.getMaxLevel());
        vo.setExpRequired(entity.getExpRequired());
        vo.setCreditsRequired(entity.getCreditsRequired());
        
        // 经验书/信用点物品按名称精确匹配 sr_item（避免 LIKE 命中多条同名记录导致取值漂移）
        if (expItem != null) {
            vo.setExpItemId(expItem.getId());
            vo.setExpItemName(expItem.getItemName());
            vo.setExpItemImage(expItem.getImage());
        }
        
        if (creditsItem != null) {
            vo.setCreditsItemId(creditsItem.getId());
            vo.setCreditsItemName(creditsItem.getItemName());
            vo.setCreditsItemImage(creditsItem.getImage());
        }
        
        return vo;
    }
}
