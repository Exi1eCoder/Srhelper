package com.quico.srhelper.service.impl;

import com.quico.srhelper.config.SrhelperCacheConstants;
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

    @Override
    public List<SrCharacterExpUpgradeVO> selectAllExpUpgrades() {
        SrCharacterExpUpgradeCache cached = (SrCharacterExpUpgradeCache) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            log.debug("selectAllExpUpgrades cached is not null, 从缓存中获取角色经验升级配置");
            return cached.getList();
        }
        log.debug("selectAllExpUpgrades cached is null, 从数据库中查询角色经验升级配置");
        List<SrCharacterExpUpgradeVO> list = expUpgradeMapper.selectAllOrderByMinLevel()
                .stream()
                .map(this::convertToVO)
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
        SrCharacterExpUpgradeVO vo = new SrCharacterExpUpgradeVO();
        vo.setId(entity.getId());
        vo.setMinLevel(entity.getMinLevel());
        vo.setMaxLevel(entity.getMaxLevel());
        vo.setExpRequired(entity.getExpRequired());
        vo.setCreditsRequired(entity.getCreditsRequired());
        
        // 查询经验书（从数据库sr_item表获取）
        SrItem expItem = findItemByName("旅行见闻");
        if (expItem != null) {
            vo.setExpItemId(expItem.getId());
            vo.setExpItemName(expItem.getItemName());
            vo.setExpItemImage(expItem.getImage());
        }
        
        // 查询信用点（从数据库sr_item表获取）
        SrItem creditsItem = findItemByName("信用点");
        if (creditsItem != null) {
            vo.setCreditsItemId(creditsItem.getId());
            vo.setCreditsItemName(creditsItem.getItemName());
            vo.setCreditsItemImage(creditsItem.getImage());
        }
        
        return vo;
    }
    
    /**
     * 根据名称查询材料
     */
    private SrItem findItemByName(String name) {
        SrItem query = new SrItem();
        query.setItemName(name);
        List<SrItem> items = srItemMapper.selectSrItemList(query);
        return items.isEmpty() ? null : items.get(0);
    }
}
