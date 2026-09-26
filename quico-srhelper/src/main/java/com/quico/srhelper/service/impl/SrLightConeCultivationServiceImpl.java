package com.quico.srhelper.service.impl;

import com.quico.common.core.redis.RedisCache;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrUserLightCone;
import com.quico.srhelper.domain.vo.SrLightConeCultivationVO;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightConeAscensionMapper;
import com.quico.srhelper.mapper.SrLightConesMapper;
import com.quico.srhelper.mapper.SrUserLightConeMapper;
import com.quico.srhelper.service.ISrLightConeCultivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 光锥养成计算Service实现类
 * 
 * @author quico
 * @date 2026-06-07
 */
@Service
@RequiredArgsConstructor
public class SrLightConeCultivationServiceImpl implements ISrLightConeCultivationService
{
    private final SrUserLightConeMapper userLightConeMapper;
    private final SrLightConesMapper lightConesMapper;
    private final SrLightConeAscensionMapper ascensionMapper;
    private final SrItemMapper itemMapper;
    private final RedisCache redisCache;
    
    private static final Integer MAX_LEVEL = 80;
    
    // 缓存相关常量
    private static final String CULTIVATION_CACHE_PREFIX = "sr:lightcone:cultivation:";
    private static final String CULTIVATION_LIGHTCONE_KEY = CULTIVATION_CACHE_PREFIX + "lightcone:%d"; // userLightConeId
    private static final String CULTIVATION_TOTAL_KEY = CULTIVATION_CACHE_PREFIX + "total:%d:%d"; // userId:targetLevel
    private static final long CACHE_EXPIRE_MINUTES = 30; // 缓存有效期30分钟
    
    // 罗马数字映射
    private static final String[] ROMAN_NUMBERS = {"I", "II", "III", "IV", "V"};

    @Override
    public SrLightConeCultivationVO calculateLightConeCultivation(Long userLightConeId, Integer targetLevel)
    {
        if (targetLevel == null || targetLevel > MAX_LEVEL)
        {
            targetLevel = MAX_LEVEL;
        }
        
        // 构建缓存key
        String cacheKey = String.format(CULTIVATION_LIGHTCONE_KEY, userLightConeId);
        
        // 尝试从缓存获取
        SrLightConeCultivationVO cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null)
        {
            return cachedResult;
        }
        
        SrUserLightCone ulc = userLightConeMapper.selectSrUserLightConeById(userLightConeId);
        if (ulc == null)
        {
            return null;
        }
        
        SrLightCones lightCone = lightConesMapper.selectSrLightConesById(ulc.getLightConeId());
        if (lightCone == null)
        {
            return null;
        }
        
        SrLightConeCultivationVO result = new SrLightConeCultivationVO();
        
        // 光锥信息
        SrLightConeCultivationVO.LightConeInfo info = new SrLightConeCultivationVO.LightConeInfo();
        info.setUserLightConeId(ulc.getId());
        info.setLightConeId(ulc.getLightConeId());
        info.setLightConeName(lightCone.getLightConeName());
        info.setLightConeImage(lightCone.getImage());
        info.setStarLevel(lightCone.getStarLevel() != null ? lightCone.getStarLevel().intValue() : 3);
        info.setCurrentLevel(ulc.getLevel() != null ? ulc.getLevel() : 1);
        info.setTargetLevel(targetLevel);
        info.setCurrentSuperimposition(ulc.getSuperimposition() != null ? ulc.getSuperimposition() : 1);
        // 设置叠影罗马数字
        int superimpositionIndex = info.getCurrentSuperimposition() - 1;
        if (superimpositionIndex >= 0 && superimpositionIndex < ROMAN_NUMBERS.length)
        {
            info.setSuperimpositionRoman(ROMAN_NUMBERS[superimpositionIndex]);
        }
        result.setLightCone(info);
        
        // 计算材料需求
        Map<Long, Long> materialMap = calculateMaterialsForLightCone(ulc, targetLevel);
        
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(ulc.getUserId());
        
        // 构建材料需求列表
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = buildMaterialList(materialMap, userItemMap);
        result.setMaterials(materialList);
        
        // 判断是否已完成培养
        result.setCompleted(materialList.isEmpty());
        
        // 缓存计算结果，有效期30分钟
        redisCache.setCacheObject(cacheKey, result, (int) CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return result;
    }

    @Override
    public SrLightConeCultivationVO calculateTotalLightConeCultivation(Long userId, Integer targetLevel)
    {
        if (targetLevel == null || targetLevel > MAX_LEVEL)
        {
            targetLevel = MAX_LEVEL;
        }
        
        // 构建缓存key
        String cacheKey = String.format(CULTIVATION_TOTAL_KEY, userId, targetLevel);
        
        // 尝试从缓存获取
        SrLightConeCultivationVO cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null)
        {
            return cachedResult;
        }
        
        SrLightConeCultivationVO result = new SrLightConeCultivationVO();
        
        // 查询用户所有光锥
        List<SrUserLightCone> userLightCones = userLightConeMapper.selectByUserIdWithDetails(userId);
        
        // 计算总体材料需求
        Map<Long, Long> totalMaterialMap = new HashMap<>();
        
        for (SrUserLightCone ulc : userLightCones)
        {
            Map<Long, Long> lightConeMaterials = calculateMaterialsForLightCone(ulc, targetLevel);
            for (Map.Entry<Long, Long> entry : lightConeMaterials.entrySet())
            {
                totalMaterialMap.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }
        
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(userId);
        
        // 构建材料需求列表
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = buildMaterialList(totalMaterialMap, userItemMap);
        result.setMaterials(materialList);
        
        // 判断是否已完成培养（所有光锥都满级）
        result.setCompleted(materialList.isEmpty());
        
        // 设置总体光锥信息（用于前端展示）
        SrLightConeCultivationVO.LightConeInfo totalInfo = new SrLightConeCultivationVO.LightConeInfo();
        totalInfo.setLightConeName("总体");
        totalInfo.setTargetLevel(targetLevel);
        result.setLightCone(totalInfo);
        
        // 缓存计算结果，有效期30分钟
        redisCache.setCacheObject(cacheKey, result, (int) CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return result;
    }

    /**
     * 计算单个光锥的材料需求
     */
    private Map<Long, Long> calculateMaterialsForLightCone(SrUserLightCone ulc, Integer targetLevel)
    {
        Map<Long, Long> materialMap = new HashMap<>();
        
        Integer currentLevel = ulc.getLevel() != null ? ulc.getLevel() : 1;
        
        // 获取光锥晋升材料配置
        List<SrLightConeAscension> ascensions = ascensionMapper.selectSrLightConeAscensionByConeId(ulc.getLightConeId());
        if (ascensions == null || ascensions.isEmpty())
        {
            return materialMap;
        }
        
        // 遍历所有晋升材料，计算需要的材料
        for (SrLightConeAscension ascension : ascensions)
        {
            Integer breakLevel = ascension.getBreakLevel();
            
            // 当前等级 < breakLevel 的材料都需要
            if (currentLevel < breakLevel && breakLevel <= targetLevel)
            {
                if (ascension.getItemId() != null && ascension.getQuantity() != null)
                {
                    materialMap.merge(ascension.getItemId(), ascension.getQuantity().longValue(), Long::sum);
                }
            }
        }
        
        return materialMap;
    }

    /**
     * 获取用户背包材料
     */
    private Map<Long, Long> getUserItemMap(Long userId)
    {
        Map<Long, Long> userItemMap = new HashMap<>();
        // 这里需要查询用户背包材料，暂时返回空Map
        // 实际实现需要查询 sr_user_item 表
        return userItemMap;
    }

    /**
     * 构建材料需求列表
     */
    private List<SrLightConeCultivationVO.MaterialRequirement> buildMaterialList(Map<Long, Long> materialMap, Map<Long, Long> userItemMap)
    {
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : materialMap.entrySet())
        {
            Long itemId = entry.getKey();
            Long requiredQuantity = entry.getValue();
            Long ownedQuantity = userItemMap.getOrDefault(itemId, 0L);
            
            SrItem item = itemMapper.selectSrItemById(itemId);
            if (item == null)
            {
                continue;
            }
            
            SrLightConeCultivationVO.MaterialRequirement requirement = new SrLightConeCultivationVO.MaterialRequirement();
            requirement.setItemId(itemId);
            requirement.setItemName(item.getItemName());
            requirement.setItemImage(item.getImage());
            requirement.setRarityLevel(item.getStarLevel() != null ? item.getStarLevel().intValue() : 2);
            requirement.setRequiredQuantity(requiredQuantity);
            requirement.setOwnedQuantity(ownedQuantity);
            requirement.setDifference(requiredQuantity - ownedQuantity);
            
            materialList.add(requirement);
        }
        
        return materialList;
    }
}