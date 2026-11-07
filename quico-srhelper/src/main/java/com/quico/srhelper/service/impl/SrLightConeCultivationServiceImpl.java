package com.quico.srhelper.service.impl;

import com.quico.srhelper.constants.ExpBookConstants;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrLightConeAscension;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrLightconeExpUpgrade;
import com.quico.srhelper.domain.SrUserItem;
import com.quico.srhelper.domain.SrUserLightCone;
import com.quico.srhelper.domain.enums.CommonItemEnum;
import com.quico.srhelper.domain.enums.MaterialSortEnum;
import com.quico.srhelper.domain.enums.SynthesisStatusEnum;
import com.quico.srhelper.domain.vo.MaterialCalculationResultVO;
import com.quico.srhelper.domain.vo.SrLightConeCultivationVO;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightConeAscensionMapper;
import com.quico.srhelper.mapper.SrLightConesMapper;
import com.quico.srhelper.mapper.SrLightconeExpUpgradeMapper;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.mapper.SrUserLightConeMapper;
import com.quico.srhelper.service.ISrLightConeCultivationService;
import com.quico.srhelper.service.helper.MaterialSynthesisCalculator;
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
    private final MaterialSynthesisCalculator synthesisCalculator;

    private static final Integer MAX_LEVEL = 80;

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

        // 计算材料消耗（晋升材料 + 升级经验/信用点，按当前等级过滤）
        List<SrLightconeExpUpgrade> expUpgrades = starLevel != null
                ? expUpgradeMapper.selectByStarLevel(starLevel) : new ArrayList<>();
        LightConeMaterialCalc calc = calculateMaterialsForLightCone(
                ulc.getLightConeId(), currentLevel, targetLevel, expUpgrades);
        log.debug("用户持有id{}的光锥{}材料消耗：{}", ulc.getId(), ulc.getLightConeId(), calc.materials);
        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(ulc.getUserId());

        // 构建材料需求列表（材料元数据一次查出，供列表与合成折算共用）
        Map<Long, SrItem> itemMap = selectItemsByIds(calc.materials.keySet());
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = buildMaterialList(calc.materials, userItemMap, itemMap);
        result.setMaterials(materialList);
        // 光锥维度的合成折算结果（材料列表提供所需量，背包 map 提供全系列持有量）
        result.setCalculation(synthesisCalculator.calculate(materialList, userItemMap, itemMap));
        // 经验书缺口折算为2星经验书
        SrItem expBook = itemMapper.selectSrItemById(ExpBookConstants.LIGHT_CONE_2_STAR_ITEM_ID);
        appendExpBookRequirement(result.getCalculation(), calc.requiredExp, userItemMap, expBook);

        // 判断是否已完成培养（无材料需求且经验书足够）
        result.setCompleted(materialList.isEmpty() && expBookSufficient(calc.requiredExp, userItemMap));

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

        // 计算总体材料消耗
        Map<Long, Long> totalMaterialMap = new HashMap<>();
        long totalRequiredExp = 0L;
        // 未完成材料绑定的光锥不参与养成计算
        Set<Long> usableLightConeIds = usableChecker.getUsableLightconeIds();
        for (SrUserLightCone ulc : userLightCones)
        {
            if (!usableLightConeIds.contains(ulc.getLightConeId())) continue;
            Integer starLevel = ulc.getStarLevel() != null ? ulc.getStarLevel().intValue() : null;
            Integer currentLevel = ulc.getLevel() != null ? ulc.getLevel() : 1;
            LightConeMaterialCalc calc = calculateMaterialsForLightCone(
                    ulc.getLightConeId(), currentLevel, targetLevel,
                    filterExpUpgradesByStar(allExpUpgrades, starLevel));
            calc.materials.forEach((itemId, quantity) -> totalMaterialMap.merge(itemId, quantity, Long::sum));
            totalRequiredExp += calc.requiredExp;
        }

        // 查询用户背包材料
        Map<Long, Long> userItemMap = getUserItemMap(userId);

        // 构建材料需求列表（材料元数据一次查出，供列表与合成折算共用）
        Map<Long, SrItem> itemMap = selectItemsByIds(totalMaterialMap.keySet());
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = buildMaterialList(totalMaterialMap, userItemMap, itemMap);
        result.setMaterials(materialList);
        // 光锥总体维度的合成折算结果（材料列表提供所需量，背包 map 提供全系列持有量）
        result.setCalculation(synthesisCalculator.calculate(materialList, userItemMap, itemMap));
        // 经验书：所有光锥的需求经验先汇总，再统一抵扣一次背包经验
        SrItem expBook = itemMapper.selectSrItemById(ExpBookConstants.LIGHT_CONE_2_STAR_ITEM_ID);
        appendExpBookRequirement(result.getCalculation(), totalRequiredExp, userItemMap, expBook);

        // 判断是否已完成培养（所有光锥都满级且经验书足够）
        result.setCompleted(materialList.isEmpty() && expBookSufficient(totalRequiredExp, userItemMap));

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
     * - 升级经验：升级表存储2星经验书数量，统一按当前等级升至80级累计为原始经验值
     *
     * @param expUpgrades 该光锥星级对应的升级配置
     */
    private LightConeMaterialCalc calculateMaterialsForLightCone(Long lightConeId, Integer currentLevel, Integer targetLevel,
                                                                List<SrLightconeExpUpgrade> expUpgrades)
    {
        LightConeMaterialCalc calc = new LightConeMaterialCalc();

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
                    calc.materials.merge(ascension.getItemId(), ascension.getQuantity().longValue(), Long::sum);
                }
            }
        }

        // 升级经验 + 信用点，经验统一按当前等级升至满级（80）统计
        calculateExpMaterials(currentLevel, MAX_LEVEL, expUpgrades, calc);

        return calc;
    }

    /**
     * 单个光锥的材料计算结果（内部传递用）
     */
    private static class LightConeMaterialCalc
    {
        private final Map<Long, Long> materials = new HashMap<>();
        private long requiredExp = 0L;
    }

    /**
     * 计算升级所需经验与信用点消耗
     * 升级表存储的是2星经验书（稀薄以太）数量，这里换算为原始经验值，由接口层统一抵扣背包后
     * 折算为2星经验书缺口，高阶经验书不进入材料消耗列表
     */
    private void calculateExpMaterials(Integer currentLevel, Integer targetLevel,
                                       List<SrLightconeExpUpgrade> expUpgrades, LightConeMaterialCalc calc)
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
            if (upgrade.getExpRequired() != null)
            {
                // 配置值 = 2星经验书数量，换算为经验值
                calc.requiredExp += upgrade.getExpRequired() * ExpBookConstants.LIGHT_CONE_2_STAR_EXP;
            }
            if (upgrade.getCreditsRequired() != null)
            {
                calc.materials.merge(CommonItemEnum.CREDIT.getItemId(), upgrade.getCreditsRequired(), Long::sum);
            }
        }
    }

    /**
     * 把经验缺口折算为2星经验书，追加到需刷取列表
     */
    private void appendExpBookRequirement(MaterialCalculationResultVO calculation, long requiredExp,
                                          Map<Long, Long> userItemMap, SrItem expBook)
    {
        if (calculation == null || requiredExp <= 0L)
        {
            return;
        }
        long ownedExp = ExpBookConstants.lightConeOwnedExp(userItemMap);
        long bookCount = ExpBookConstants.shortageBookCount(requiredExp, ownedExp,
                ExpBookConstants.LIGHT_CONE_2_STAR_EXP);
        if (bookCount <= 0L)
        {
            return;
        }

        MaterialCalculationResultVO.RequiredItem requiredItem = new MaterialCalculationResultVO.RequiredItem();
        MaterialCalculationResultVO.ItemInfo itemInfo = new MaterialCalculationResultVO.ItemInfo();
        itemInfo.setItemId(expBook != null ? expBook.getId() : ExpBookConstants.LIGHT_CONE_2_STAR_ITEM_ID);
        itemInfo.setItemImage(expBook != null ? expBook.getImage() : null);
        requiredItem.setItem(itemInfo);
        requiredItem.setDifference(bookCount);
        requiredItem.setItemType(ExpBookConstants.ITEM_TYPE_EXP_BOOK);

        if (calculation.getRequired() == null)
        {
            calculation.setRequired(new ArrayList<>());
        }
        calculation.getRequired().add(requiredItem);
        // 经验书只能刷取、无法合成，按"追加一项必须刷取的需求"合并整体状态
        SynthesisStatusEnum status = calculation.getStatus() != null
                ? calculation.getStatus() : SynthesisStatusEnum.ENOUGH;
        calculation.setStatus(status.withFarmRequired());
    }

    /**
     * 背包经验书是否足够覆盖需求经验
     */
    private boolean expBookSufficient(long requiredExp, Map<Long, Long> userItemMap)
    {
        return requiredExp <= ExpBookConstants.lightConeOwnedExp(userItemMap);
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
                                                                                Map<Long, Long> userItemMap,
                                                                                Map<Long, SrItem> itemMap)
    {
        List<SrLightConeCultivationVO.MaterialRequirement> materialList = new ArrayList<>();
        if (materialMap == null || materialMap.isEmpty())
        {
            return materialList;
        }

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
