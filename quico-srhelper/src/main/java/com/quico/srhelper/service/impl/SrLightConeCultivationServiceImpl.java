package com.quico.srhelper.service.impl;

import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrLightconeExpUpgrade;
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.domain.SrUserLightCone;
import com.quico.srhelper.domain.enums.CommonItemEnum;
import com.quico.srhelper.domain.enums.MaterialSortEnum;
import com.quico.srhelper.domain.vo.SrLightConeCultivationVO;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightConeAscensionMapper;
import com.quico.srhelper.mapper.SrLightConesMapper;
import com.quico.srhelper.mapper.SrLightconeExpUpgradeMapper;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.mapper.SrUserLightConeMapper;
import com.quico.srhelper.service.ISrLightConeCultivationService;
import com.quico.srhelper.service.helper.UsableChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 光锥养成计算Service实现类
 *
 * @author quico
 * @date 2026-06-07
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrLightConeCultivationServiceImpl implements ISrLightConeCultivationService
{
    private final SrUserLightConeMapper userLightConeMapper;
    private final SrLightConesMapper lightConesMapper;
    private final SrLightConeAscensionMapper ascensionMapper;
    private final SrLightconeExpUpgradeMapper expUpgradeMapper;
    private final SrItemMapper itemMapper;
    private final SrUserItemMapper userItemMapper;
    private final UsableChecker usableChecker;

    private static final Integer MAX_LEVEL = 80;

    /** 光锥升级经验书物品名（升级表直接存储其数量） */
    private static final String EXP_BOOK_NAME = "稀薄以太";

    // 罗马数字映射
    private static final String[] ROMAN_NUMBERS = {"I", "II", "III", "IV", "V"};

    @Override
    public SrLightConeCultivationVO calculateLightConeCultivation(Long userLightConeId, Integer targetLevel)
    {
        if (targetLevel == null || targetLevel > MAX_LEVEL)
        {
            targetLevel = MAX_LEVEL;
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

        // 兜底校验：材料绑定未完成（非6行）的光锥不可进行养成计算
        usableChecker.assertLightconeUsable(ulc.getLightConeId());

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

        Integer starLevel = lightCone.getStarLevel() != null ? lightCone.getStarLevel().intValue() : null;
        Integer currentLevel = ulc.getLevel() != null ? ulc.getLevel() : 1;

        // 计算材料消耗（晋升材料 + 升级经验书/信用点，按当前等级过滤）
        List<SrLightconeExpUpgrade> expUpgrades = starLevel != null
                ? expUpgradeMapper.selectByStarLevel(starLevel) : new ArrayList<>();
        Map<Long, Long> materialMap = calculateMaterialsForLightCone(
                ulc.getLightConeId(), currentLevel, targetLevel, expUpgrades, resolveExpBookItemId());
        log.debug("用户持有id{}的光锥{}材料消耗：{}", ulc.getId(), ulc.getLightConeId(), materialMap);
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(ulc.getUserId());

        // 构建材料需求列表
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = buildMaterialList(materialMap, userItemMap);
        result.setMaterials(materialList);

        // 判断是否已完成培养（无材料需求）
        result.setCompleted(materialList.isEmpty());

        log.debug("光锥{}养成材料计算结果：{}", ulc.getLightConeId(), result);
        return result;
    }

    @Override
    public SrLightConeCultivationVO calculateTotalLightConeCultivation(Long userId, Integer targetLevel)
    {
        if (targetLevel == null || targetLevel > MAX_LEVEL)
        {
            targetLevel = MAX_LEVEL;
        }

        SrLightConeCultivationVO result = new SrLightConeCultivationVO();

        // 查询用户所有光锥（养成计算不需要过滤条件）
        List<SrUserLightCone> userLightCones = userLightConeMapper.selectByUserIdWithDetails(userId, null, null, null);

        // 全量升级配置一次性查出，供各光锥按星级过滤（避免逐个光锥查库）
        List<SrLightconeExpUpgrade> allExpUpgrades = expUpgradeMapper.selectAllOrderByStarAndLevel();
        Long expItemId = resolveExpBookItemId();

        // 计算总体材料消耗
        Map<Long, Long> totalMaterialMap = new HashMap<>();
        // 未完成材料绑定的光锥不参与养成计算
        Set<Long> usableLightConeIds = usableChecker.getUsableLightconeIds();
        for (SrUserLightCone ulc : userLightCones)
        {
            if (!usableLightConeIds.contains(ulc.getLightConeId())) continue;
            Integer starLevel = ulc.getStarLevel() != null ? ulc.getStarLevel().intValue() : null;
            Integer currentLevel = ulc.getLevel() != null ? ulc.getLevel() : 1;
            Map<Long, Long> lightConeMaterials = calculateMaterialsForLightCone(
                    ulc.getLightConeId(), currentLevel, targetLevel,
                    filterExpUpgradesByStar(allExpUpgrades, starLevel), expItemId);
            lightConeMaterials.forEach((itemId, quantity) -> totalMaterialMap.merge(itemId, quantity, Long::sum));
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

        log.debug("用户{}的光锥养成材料计算结果：{}", userId, result);
        return result;
    }

    /**
     * 计算单个光锥的材料消耗（按当前等级过滤）
     * - 晋升材料：break_level（20/30/40/50/60/70/80）大于当前等级且不超过目标等级的才需要
     * - 升级经验：升级表直接存储经验书数量，区间覆盖当前等级之后到目标等级的部分才需要
     *
     * @param expUpgrades 该光锥星级对应的升级配置
     * @param expItemId   经验书物品ID，可为 null
     */
    private Map<Long, Long> calculateMaterialsForLightCone(Long lightConeId, Integer currentLevel, Integer targetLevel,
                                                           List<SrLightconeExpUpgrade> expUpgrades, Long expItemId)
    {
        Map<Long, Long> materialMap = new HashMap<>();

        // 晋升材料
        List<SrLightConeAscension> ascensions = ascensionMapper.selectSrLightConeAscensionByConeId(lightConeId);
        if (ascensions != null)
        {
            for (SrLightConeAscension ascension : ascensions)
            {
                Integer breakLevel = ascension.getBreakLevel();
                if (breakLevel == null || ascension.getItemId() == null || ascension.getQuantity() == null)
                {
                    continue;
                }
                // 光锥突破：0-20级为第一次突破，之后每10级一次，共6次（20/30/40/50/60/70），80级不再突破
                // 突破等级 >= 当前等级 且 未超过目标等级 时需要（80级无突破）
                if (breakLevel >= currentLevel && breakLevel <= targetLevel && breakLevel < MAX_LEVEL)
                {
                    materialMap.merge(ascension.getItemId(), ascension.getQuantity().longValue(), Long::sum);
                }
            }
        }

        // 升级经验书 + 信用点
        calculateExpMaterials(currentLevel, targetLevel, expUpgrades, expItemId, materialMap);

        return materialMap;
    }

    /**
     * 计算升级经验书与信用点消耗
     * 升级表直接存储经验书（稀薄以太）数量，无需换算
     */
    private void calculateExpMaterials(Integer currentLevel, Integer targetLevel,
                                       List<SrLightconeExpUpgrade> expUpgrades, Long expItemId,
                                       Map<Long, Long> materialMap)
    {
        // 目标等级与当前等级相同，不需要升级经验和信用点
        if (expUpgrades == null || expUpgrades.isEmpty() || currentLevel == null
                || currentLevel.equals(targetLevel))
        {
            return;
        }

        for (SrLightconeExpUpgrade upgrade : expUpgrades)
        {
            // 只计算当前等级之后需要的经验
            if (upgrade.getMinLevel() == null || upgrade.getMaxLevel() == null
                    || upgrade.getMinLevel() < currentLevel || upgrade.getMaxLevel() > targetLevel)
            {
                continue;
            }
            if (expItemId != null && upgrade.getExpRequired() != null)
            {
                materialMap.merge(expItemId, upgrade.getExpRequired(), Long::sum);
            }
            if (upgrade.getCreditsRequired() != null)
            {
                materialMap.merge(CommonItemEnum.CREDIT.getItemId(), upgrade.getCreditsRequired(), Long::sum);
            }
        }
    }

    /**
     * 按星级过滤升级配置
     */
    private List<SrLightconeExpUpgrade> filterExpUpgradesByStar(List<SrLightconeExpUpgrade> all, Integer starLevel)
    {
        List<SrLightconeExpUpgrade> result = new ArrayList<>();
        if (all == null || starLevel == null)
        {
            return result;
        }
        for (SrLightconeExpUpgrade upgrade : all)
        {
            if (starLevel.equals(upgrade.getStarLevel()))
            {
                result.add(upgrade);
            }
        }
        return result;
    }

    /**
     * 解析光锥升级经验书（稀薄以太）物品ID
     */
    private Long resolveExpBookItemId()
    {
        SrItem item = itemMapper.selectSrItemByName(EXP_BOOK_NAME);
        return item != null ? item.getId() : null;
    }

    /**
     * 获取用户背包材料数量映射
     */
    private Map<Long, Long> getUserItemMap(Long userId)
    {
        Map<Long, Long> userItemMap = new HashMap<>();
        SrUserItem query = new SrUserItem();
        query.setUserId(userId);
        List<SrUserItem> items = userItemMapper.selectSrUserItemList(query);
        for (SrUserItem item : items)
        {
            if (item.getItemId() != null && item.getQuantity() != null)
            {
                userItemMap.put(item.getItemId(), item.getQuantity());
            }
        }
        return userItemMap;
    }

    /**
     * 构建材料需求列表
     * - requiredQuantity（消耗）：按当前等级进度过滤后的材料消耗
     * - difference（差额）：消耗 - 用户持有数量，不足为0
     */
    private List<SrLightConeCultivationVO.MaterialRequirement> buildMaterialList(Map<Long, Long> materialMap,
                                                                                 Map<Long, Long> userItemMap)
    {
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = new ArrayList<>();
        if (materialMap == null || materialMap.isEmpty())
        {
            return materialList;
        }

        // 收集 itemId 后一次性批量查询，避免逐条查库
        Map<Long, SrItem> itemMap = selectItemsByIds(materialMap.keySet());

        for (Map.Entry<Long, Long> entry : materialMap.entrySet())
        {
            Long itemId = entry.getKey();
            SrItem item = itemMap.get(itemId);
            if (item == null)
            {
                continue;
            }

            long requiredQuantity = entry.getValue();
            long ownedQuantity = userItemMap.getOrDefault(itemId, 0L);

            SrLightConeCultivationVO.MaterialRequirement requirement = new SrLightConeCultivationVO.MaterialRequirement();
            requirement.setItemId(itemId);
            requirement.setItemName(item.getItemName());
            requirement.setItemImage(item.getImage());
            requirement.setRarityLevel(item.getStarLevel() != null ? item.getStarLevel().intValue() : 2);
            requirement.setRequiredQuantity(requiredQuantity);
            requirement.setOwnedQuantity(ownedQuantity);
            // 差额 = 材料消耗 - 用户持有数量，不足为0
            requirement.setDifference(Math.max(requiredQuantity - ownedQuantity, 0L));
            // 材料类型排序优先级（内部使用，不返回前端）
            requirement.setSortOrder(MaterialSortEnum.resolveOrder(item));

            materialList.add(requirement);
        }

        // 按材料类型优先级（信用点/经验书/TOD/EOW/SS/TRA/CAL）、星级从高到低、itemId 稳定排序
        materialList.sort(Comparator
                .comparingInt(SrLightConeCultivationVO.MaterialRequirement::getSortOrder)
                .thenComparing(SrLightConeCultivationVO.MaterialRequirement::getRarityLevel,
                        Comparator.nullsLast(Comparator.<Integer>reverseOrder()))
                .thenComparing(SrLightConeCultivationVO.MaterialRequirement::getItemId));

        return materialList;
    }

    /**
     * 按 itemId 集合批量查询材料，返回 itemId -> SrItem 映射
     */
    private Map<Long, SrItem> selectItemsByIds(Set<Long> itemIds)
    {
        Map<Long, SrItem> itemMap = new HashMap<>();
        if (itemIds == null || itemIds.isEmpty())
        {
            return itemMap;
        }
        List<SrItem> items = itemMapper.selectSrItemByIds(new ArrayList<>(itemIds));
        for (SrItem item : items)
        {
            itemMap.put(item.getId(), item);
        }
        return itemMap;
    }
}
