package com.quico.srhelper.service.impl;

import com.quico.srhelper.config.SrhelperCacheConstants;
import com.quico.srhelper.domain.SrCharacterExpUpgrade;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.dto.SrCharacterExpUpgradeDTO;

import com.quico.srhelper.domain.vo.SrCharacterExpUpgradeVO;
import com.quico.srhelper.mapper.SrCharacterExpUpgradeMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.service.ISrCharacterExpUpgradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色经验升级Service实现类
 */
@Service
@RequiredArgsConstructor
public class SrCharacterExpUpgradeServiceImpl implements ISrCharacterExpUpgradeService {
    
    private final SrCharacterExpUpgradeMapper expUpgradeMapper;
    private final SrItemMapper srItemMapper;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String CACHE_KEY = SrhelperCacheConstants.CHAR_EXP_UPGRADE_KEY + "list";

    @Override
    public List<SrCharacterExpUpgradeVO> selectAllExpUpgrades() {
        @SuppressWarnings("unchecked")
        List<SrCharacterExpUpgradeVO> cached = (List<SrCharacterExpUpgradeVO>) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }
        List<SrCharacterExpUpgradeVO> list = expUpgradeMapper.selectAllOrderByMinLevel()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(CACHE_KEY, list, SrhelperCacheConstants.TTL_CONFIG, TimeUnit.MINUTES);
        return list;
    }
    
    @Override
    public SrCharacterExpUpgradeVO selectExpUpgradeById(Long id) {
        SrCharacterExpUpgrade entity = expUpgradeMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }
    
    @Override
    public SrCharacterExpUpgradeVO selectExpUpgradeByLevel(Integer level) {
        SrCharacterExpUpgrade entity = expUpgradeMapper.selectByLevel(level);
        return entity != null ? convertToVO(entity) : null;
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
        redisTemplate.delete(CACHE_KEY);
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
