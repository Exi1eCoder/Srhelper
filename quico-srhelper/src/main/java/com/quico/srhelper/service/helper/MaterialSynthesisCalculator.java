package com.quico.srhelper.service.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.enums.ItemAscensionTypeEnum;
import com.quico.srhelper.domain.enums.SynthesisStatusEnum;
import com.quico.srhelper.domain.vo.MaterialCalculationResultVO;
import com.quico.srhelper.domain.contract.MaterialRequirementView;
import com.quico.srhelper.mapper.SrItemMapper;

import lombok.RequiredArgsConstructor;

/**
 * 材料合成折算计算器（角色、光锥养成计算共用）
 *
 * 游戏中 3 个低阶材料可合成 1 个高阶材料（仅 TRA 世界掉落 / CAL 拟造花萼，
 * 二星→三星→四星，不支持高阶转低阶）。同一 series 内各稀有度视为整体，
 * 按"低阶自身需求优先、富余才能合成高阶"的口径从低阶向高阶逐级累积富余量，
 * 据此判断高阶缺口能否由合成补足。
 *
 * 本计算器按"角色/光锥维度"汇总该单位的全部材料，输出整体状态（取最坏情况）、
 * 折算后仍需刷取的材料及各自差额、以及各目标材料的合成方案列表。
 *
 * 状态口径（枚举声明顺序即严重程度升序，聚合取最坏）：
 * - 完全够（ENOUGH）：持有量已满足自身需求，无需合成；
 * - 通过合成足够（ENOUGH_BY_SYNTHESIS）：自身持有不足，但低阶富余可合成补足；
 * - 合成一部分仍不够（PARTIAL_SYNTHESIS）：低阶仅能补足一部分，仍有缺口需刷取；
 * - 需刷取（NEED_FARM）：低阶不足以合成，缺口全部需刷取。
 *
 * @author quico
 */
@Component
@RequiredArgsConstructor
public class MaterialSynthesisCalculator {

    /** 低阶合成高阶的固定比例：3 个低阶 = 1 个高阶 */
    private static final int SYNTHESIS_RATIO = 3;

    private final SrItemMapper itemMapper;

    /**
     * 汇总计算某角色/光锥全部材料的合成折算结果
     *
     * @param materials   材料需求列表（角色/光锥维度的 materialList，提供所需数量与"本次需要哪些材料"）
     * @param userItemMap 用户背包完整持有 map（itemId -> 持有数量），必须覆盖同系列全部低阶材料
     * @param itemMap     材料元数据 map（itemId -> SrItem），需覆盖 materials 中全部 itemId
     * @return 角色/光锥维度的折算结果（整体状态 + 需求列表 + 合成方案列表）
     */
    public MaterialCalculationResultVO calculate(List<? extends MaterialRequirementView> materials,
                                               Map<Long, Long> userItemMap,
                                               Map<Long, SrItem> itemMap) {
        return calculate(materials, userItemMap, itemMap, new HashMap<>());
    }

    /**
     * 汇总计算某角色/光锥全部材料的合成折算结果（带系列缓存，跨多次调用复用避免重复查库）
     *
     * @param seriesCache 同系列材料缓存（seriesId -> 系列内按星级升序的材料），可为 null
     */
    public MaterialCalculationResultVO calculate(List<? extends MaterialRequirementView> materials,
                                               Map<Long, Long> userItemMap,
                                               Map<Long, SrItem> itemMap,
                                               Map<Long, List<SrItem>> seriesCache) {
        MaterialCalculationResultVO result = new MaterialCalculationResultVO();
        // 聚合起点：无缺口
        result.setStatus(SynthesisStatusEnum.ENOUGH);
        result.setRequired(new ArrayList<>());
        if (materials == null || materials.isEmpty() || itemMap == null) {
            return result;
        }
        if (userItemMap == null) {
            userItemMap = Collections.emptyMap();
        }

        // 从材料需求列表派生所需数量 map（同时确定"本次需要哪些材料"）
        // 持有量不能从列表派生：列表中只有本次消耗的材料，而同系列低阶材料
        // （如角色只需四星时的二星、三星）不在列表内，但它们恰恰是向上合成的来源。
        Map<Long, Long> materialMap = new LinkedHashMap<>();
        for (MaterialRequirementView material : materials) {
            if (material == null || material.getItemId() == null) {
                continue;
            }
            materialMap.put(material.getItemId(),
                    material.getRequiredQuantity() != null ? material.getRequiredQuantity() : 0L);
        }

        List<MaterialCalculationResultVO.RequiredItem> requiredItems = new ArrayList<>();
        List<MaterialCalculationResultVO.Synthesis> plans = new ArrayList<>();

        // 按 seriesId 归集可合成材料（TRA/CAL），其余材料无法合成、单独判定
        Map<Long, List<Long>> seriesIds = new LinkedHashMap<>();
        List<Long> plainItemIds = new ArrayList<>();
        for (Long itemId : materialMap.keySet()) {
            SrItem item = itemMap.get(itemId);
            if (item == null) {
                continue;
            }
            if (isSynthesizable(item)) {
                seriesIds.computeIfAbsent(item.getSeriesId(), k -> new ArrayList<>()).add(itemId);
            } else {
                plainItemIds.add(itemId);
            }
        }

        // 不可合成的材料：直接按（所需 - 持有）判定，有缺口即计入需刷取清单
        for (Long itemId : plainItemIds) {
            long shortQty = Math.max(materialMap.getOrDefault(itemId, 0L)
                    - userItemMap.getOrDefault(itemId, 0L), 0L);
            if (shortQty > 0) {
                requiredItems.add(buildRequired(itemMap.get(itemId), shortQty));
                result.setStatus(worse(result.getStatus(), SynthesisStatusEnum.NEED_FARM));
            }
        }

        // 可合成的材料：按 series 整体逐级折算后汇总
        for (Long seriesId : seriesIds.keySet()) {
            SeriesOutcome outcome = evaluateSeries(loadSeriesItems(seriesId, seriesCache), materialMap, userItemMap);
            requiredItems.addAll(outcome.requiredItems);
            result.setStatus(worse(result.getStatus(), outcome.status));
            plans.addAll(outcome.plans);
        }

        result.setRequired(requiredItems);
        result.setSynthesis(plans.isEmpty() ? null : plans);
        return result;
    }

    /**
     * 单个 series 整体折算
     * 按稀有度升序遍历，逐级累积"由低阶合成而来"的可用量：
     * 每层先满足自身需求，富余部分按 3:1 合成上一层。
     * 允许越级合成：中间阶无持有时，可由更低阶逐级折算（如 9 个二星合成 1 个四星）。
     */
    private SeriesOutcome evaluateSeries(List<SrItem> seriesItems, Map<Long, Long> materialMap,
                                         Map<Long, Long> userItemMap) {
        SeriesOutcome outcome = new SeriesOutcome();
        if (seriesItems == null || seriesItems.isEmpty()) {
            return outcome;
        }
        // 兜底按星级升序，保证低阶先于高阶处理
        List<SrItem> ordered = new ArrayList<>(seriesItems);
        ordered.sort(Comparator.comparing(SrItem::getStarLevel,
                Comparator.nullsLast(Comparator.<Long>naturalOrder())));

        // 系列最低阶材料：越级合成方案统一折算到最低阶表示
        SrItem lowestItem = null;
        for (SrItem candidate : ordered) {
            if (candidate != null && candidate.getId() != null) {
                lowestItem = candidate;
                break;
            }
        }

        // 上一层：由更低阶富余合成出来的数量
        int producedFromBelow = 0;
        for (SrItem item : ordered) {
            if (item == null || item.getId() == null) {
                continue;
            }
            long requiredQty = materialMap.getOrDefault(item.getId(), 0L);
            long owned = userItemMap.getOrDefault(item.getId(), 0L);
            long available = owned + producedFromBelow;
            long shortQty = Math.max(requiredQty - available, 0L);
            long surplus = Math.max(available - requiredQty, 0L);

            // 仅本次真正需要的材料才参与状态判定与方案产出，纯富余传导的中间层不作为目标
            if (materialMap.containsKey(item.getId())) {
                // 自身需求中由低阶合成补足的数量
                int synthesizeCount = (int) Math.min(Math.max(requiredQty - owned, 0L), producedFromBelow);
                if (shortQty > 0) {
                    outcome.requiredItems.add(buildRequired(item, shortQty));
                    outcome.status = worse(outcome.status, synthesizeCount > 0
                            ? SynthesisStatusEnum.PARTIAL_SYNTHESIS : SynthesisStatusEnum.NEED_FARM);
                } else {
                    outcome.status = worse(outcome.status, synthesizeCount > 0
                            ? SynthesisStatusEnum.ENOUGH_BY_SYNTHESIS : SynthesisStatusEnum.ENOUGH);
                }
                if (synthesizeCount > 0 && lowestItem != null) {
                    outcome.plans.add(buildPlan(item, synthesizeCount, lowestItem));
                }
            }

            // 本层富余可向上合成出的高阶数量（向下取整）
            producedFromBelow = (int) (surplus / SYNTHESIS_RATIO);
        }
        return outcome;
    }

    /**
     * 组装一条需要刷取的材料（折算后差额）
     */
    private MaterialCalculationResultVO.RequiredItem buildRequired(SrItem item, long difference) {
        MaterialCalculationResultVO.RequiredItem requiredItem = new MaterialCalculationResultVO.RequiredItem();
        requiredItem.setItem(buildItemInfo(item));
        requiredItem.setDifference(difference);
        return requiredItem;
    }

    /**
     * 组装材料基础信息（物品ID + 图标URL）
     */
    private MaterialCalculationResultVO.ItemInfo buildItemInfo(SrItem item) {
        MaterialCalculationResultVO.ItemInfo itemInfo = new MaterialCalculationResultVO.ItemInfo();
        itemInfo.setItemId(item.getId());
        itemInfo.setItemImage(item.getImage());
        return itemInfo;
    }

    /**
     * 组装单条合成方案
     * 消耗材料统一折算到该系列最低阶表示：越级合成时按 3^阶差 放大消耗数量
     * （如合成 1 个四星，中间无三星时需 3^2 = 9 个二星）
     */
    private MaterialCalculationResultVO.Synthesis buildPlan(SrItem targetItem, int synthesizeCount, SrItem lowestItem) {
        MaterialCalculationResultVO.Synthesis plan = new MaterialCalculationResultVO.Synthesis();
        plan.setTargetItem(buildItemInfo(targetItem));
        plan.setSynthesizeCount(synthesizeCount);

        // 目标阶与最低阶的星级差，决定折算倍数（每高一阶 ×3）
        long levelGap = (targetItem.getStarLevel() != null && lowestItem.getStarLevel() != null)
                ? targetItem.getStarLevel() - lowestItem.getStarLevel() : 1L;
        long ratio = 1L;
        for (long i = 0; i < levelGap; i++) {
            ratio *= SYNTHESIS_RATIO;
        }

        MaterialCalculationResultVO.SynthesisMaterial material = new MaterialCalculationResultVO.SynthesisMaterial();
        material.setItem(buildItemInfo(lowestItem));
        material.setConsumeCount(synthesizeCount * (int) ratio);
        plan.setMaterials(Collections.singletonList(material));

        return plan;
    }

    /**
     * 加载同系列材料（带缓存）
     */
    private List<SrItem> loadSeriesItems(Long seriesId, Map<Long, List<SrItem>> seriesCache) {
        if (seriesCache != null && seriesCache.containsKey(seriesId)) {
            return seriesCache.get(seriesId);
        }
        List<SrItem> items = itemMapper.selectBySeriesId(seriesId);
        if (items == null) {
            items = Collections.emptyList();
        }
        if (seriesCache != null) {
            seriesCache.put(seriesId, items);
        }
        return items;
    }

    /**
     * 是否可参与合成折算：TRA/CAL 且已绑定 seriesId
     */
    private boolean isSynthesizable(SrItem item) {
        if (item == null || item.getAscensionType() == null || item.getSeriesId() == null) {
            return false;
        }
        ItemAscensionTypeEnum type = ItemAscensionTypeEnum.fromCode(item.getAscensionType().toUpperCase());
        return type != null && type.isCombinable();
    }

    /**
     * 取两者中更严重者（枚举声明顺序即严重程度升序）
     */
    private SynthesisStatusEnum worse(SynthesisStatusEnum a, SynthesisStatusEnum b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    /**
     * 单个 series 的折算中间结果
     */
    private static class SeriesOutcome {
        /** 该 series 内最坏状态 */
        private SynthesisStatusEnum status = SynthesisStatusEnum.ENOUGH;
        /** 该 series 内折算后仍需刷取的材料及差额 */
        private final List<MaterialCalculationResultVO.RequiredItem> requiredItems = new ArrayList<>();
        /** 该 series 产出的合成方案 */
        private final List<MaterialCalculationResultVO.Synthesis> plans = new ArrayList<>();
    }
}
