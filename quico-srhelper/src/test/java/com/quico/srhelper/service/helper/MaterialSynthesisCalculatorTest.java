package com.quico.srhelper.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.contract.MaterialRequirementView;
import com.quico.srhelper.domain.enums.SynthesisStatusEnum;
import com.quico.srhelper.domain.vo.MaterialCalculationResultVO;
import com.quico.srhelper.mapper.SrItemMapper;

/**
 * MaterialSynthesisCalculator 整体状态判定规则单元测试
 *
 * 锁定类注释中的整体状态口径（组合判定，而非取最坏值）：
 * - ENOUGH：无任何缺口；
 * - ENOUGH_BY_SYNTHESIS：存在合成方案，且合成后无缺口；
 * - PARTIAL_SYNTHESIS：存在合成方案，但仍有材料需刷取；
 * - NEED_FARM：不存在任何合成方案，缺口全部需刷取。
 *
 * 关键回归点：只要"有合成方案 + 仍有刷取缺口"就必须是 PARTIAL_SYNTHESIS。
 * 若有人把逻辑改回"整体取最坏值"（只要有一条不可合成材料缺口就 NEED_FARM），
 * 本类的 PARTIAL_SYNTHESIS 用例会失败。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("材料合成整体状态判定规则")
class MaterialSynthesisCalculatorTest {

    /** 可合成系列（TRA） */
    private static final long SERIES_ID = 100L;
    private static final long STAR_2_ITEM = 1L;
    private static final long STAR_3_ITEM = 2L;
    private static final long STAR_4_ITEM = 3L;

    /** 第二个可合成系列（TRA），用于验证多系列聚合 */
    private static final long SERIES_2_ID = 200L;
    private static final long SERIES_2_STAR_2_ITEM = 4L;
    private static final long SERIES_2_STAR_3_ITEM = 5L;
    private static final long SERIES_2_STAR_4_ITEM = 6L;

    /** 不可合成材料（如信用点、非 TRA/CAL 材料） */
    private static final long PLAIN_ITEM = 10L;

    @Mock
    private SrItemMapper itemMapper;

    @InjectMocks
    private MaterialSynthesisCalculator calculator;

    @Test
    @DisplayName("无缺口且无合成方案 → ENOUGH")
    void shouldReturnEnoughWhenNoShortageAndNoSynthesis() {
        stubSeries();

        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_2_ITEM, 3, 5)),
                Map.of(STAR_2_ITEM, 5L),
                itemMap(star2(), star3(), star4()));

        assertEquals(SynthesisStatusEnum.ENOUGH, result.getStatus());
        assertNull(result.getSynthesis());
        assertTrue(result.getRequired().isEmpty());
    }

    @Test
    @DisplayName("有合成方案且合成后无缺口 → ENOUGH_BY_SYNTHESIS")
    void shouldReturnEnoughBySynthesisWhenSynthesisCoversAllShortage() {
        stubSeries();

        // 需要 2 个四星，持有 18 个二星；18 = 2 * 3^2，可完全合成
        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_4_ITEM, 2, 0)),
                Map.of(STAR_2_ITEM, 18L),
                itemMap(star2(), star3(), star4()));

        assertEquals(SynthesisStatusEnum.ENOUGH_BY_SYNTHESIS, result.getStatus());
        assertTrue(result.getRequired().isEmpty());
        assertNotNull(result.getSynthesis());
        assertEquals(1, result.getSynthesis().size());
        assertEquals(2, result.getSynthesis().get(0).getSynthesizeCount().intValue());
        assertEquals(18, result.getSynthesis().get(0).getMaterials().get(0).getConsumeCount().intValue());
    }

    @Test
    @DisplayName("有合成方案但仍有刷取缺口 → PARTIAL_SYNTHESIS（同一系列合成一部分）")
    void shouldReturnPartialSynthesisWhenSeriesOnlyPartlyCovered() {
        stubSeries();

        // 需要 3 个四星，18 个二星只能合成 2 个，仍缺 1 个
        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_4_ITEM, 3, 0)),
                Map.of(STAR_2_ITEM, 18L),
                itemMap(star2(), star3(), star4()));

        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS, result.getStatus());
        assertNotNull(result.getSynthesis());
        assertFalse(result.getRequired().isEmpty());
        assertDifference(STAR_4_ITEM, 1L, result);
    }

    @Test
    @DisplayName("有合成方案 + 其他不可合成材料缺口 → PARTIAL_SYNTHESIS（不得回退为 NEED_FARM）")
    void shouldReturnPartialSynthesisWhenSynthesisExistsButOtherMaterialNeedsFarm() {
        stubSeries();

        // 四星可由二星完全合成（有方案），但不可合成材料仍缺 4 个（需刷取）
        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_4_ITEM, 2, 0), new TestMaterial(PLAIN_ITEM, 5, 1)),
                Map.of(STAR_2_ITEM, 18L, PLAIN_ITEM, 1L),
                itemMap(star2(), star3(), star4(), plainItem()));

        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS, result.getStatus());
        assertNotNull(result.getSynthesis());
        assertFalse(result.getRequired().isEmpty());
        assertEquals(1, result.getRequired().size());
        assertEquals(PLAIN_ITEM, result.getRequired().get(0).getItem().getItemId().longValue());
        assertEquals(4, result.getRequired().get(0).getDifference().longValue());
    }

    @Test
    @DisplayName("无合成方案且存在缺口 → NEED_FARM")
    void shouldReturnNeedFarmWhenNoSynthesisAndShortage() {
        // 只有不可合成材料，且持有量为 0，无任何合成方案
        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(PLAIN_ITEM, 5, 0)),
                Map.of(),
                itemMap(plainItem()));

        assertEquals(SynthesisStatusEnum.NEED_FARM, result.getStatus());
        assertNull(result.getSynthesis());
        assertEquals(1, result.getRequired().size());
        assertEquals(5, result.getRequired().get(0).getDifference().longValue());
    }

    @Test
    @DisplayName("可合成系列无任何低阶富余 → NEED_FARM")
    void shouldReturnNeedFarmWhenSeriesHasNoSurplusToSynthesize() {
        stubSeries();

        // 需要 2 个四星，但二三阶均无持有，无法合成任何数量
        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_4_ITEM, 2, 0)),
                Map.of(),
                itemMap(star2(), star3(), star4()));

        assertEquals(SynthesisStatusEnum.NEED_FARM, result.getStatus());
        assertNull(result.getSynthesis());
        assertEquals(1, result.getRequired().size());
        assertEquals(2, result.getRequired().get(0).getDifference().longValue());
    }

    @Test
    @DisplayName("多系列：一个可合成、另一个需刷取 → PARTIAL_SYNTHESIS")
    void shouldReturnPartialSynthesisWhenOneSeriesSynthesizedAndAnotherNeedsFarm() {
        stubSeries();
        when(itemMapper.selectBySeriesId(SERIES_2_ID))
                .thenReturn(Arrays.asList(series2Star2(), series2Star3(), series2Star4()));

        // 系列1 的四星可由二星完全合成；系列2 的四星无任何低阶来源，需刷取
        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_4_ITEM, 2, 0),
                        new TestMaterial(SERIES_2_STAR_4_ITEM, 1, 0)),
                Map.of(STAR_2_ITEM, 18L),
                itemMap(star2(), star3(), star4(), series2Star2(), series2Star3(), series2Star4()));

        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS, result.getStatus());
        assertNotNull(result.getSynthesis());
        assertEquals(1, result.getSynthesis().size());
        assertDifference(SERIES_2_STAR_4_ITEM, 1L, result);
    }

    @Test
    @DisplayName("userItemMap 为 null 时按无持有处理，不得抛异常")
    void shouldTreatNullUserItemMapAsEmpty() {
        stubSeries();

        MaterialCalculationResultVO result = calculator.calculate(
                List.of(new TestMaterial(STAR_4_ITEM, 2, 0)),
                null,
                itemMap(star2(), star3(), star4()));

        assertEquals(SynthesisStatusEnum.NEED_FARM, result.getStatus());
        assertNull(result.getSynthesis());
        assertEquals(2, result.getRequired().get(0).getDifference().longValue());
    }

    /**
     * 断言指定材料在 required 列表中的折算后差额
     */
    private void assertDifference(long itemId, long expectedDifference, MaterialCalculationResultVO result) {
        long actual = result.getRequired().stream()
                .filter(required -> required.getItem() != null
                        && required.getItem().getItemId() != null
                        && required.getItem().getItemId().longValue() == itemId)
                .mapToLong(required -> required.getDifference() == null ? 0L : required.getDifference())
                .findFirst()
                .orElse(-1L);
        assertEquals(expectedDifference, actual);
    }

    private void stubSeries() {
        when(itemMapper.selectBySeriesId(SERIES_ID))
                .thenReturn(Arrays.asList(star2(), star3(), star4()));
    }

    private static SrItem star2() {
        return item(STAR_2_ITEM, 2L, "TRA", SERIES_ID);
    }

    private static SrItem star3() {
        return item(STAR_3_ITEM, 3L, "TRA", SERIES_ID);
    }

    private static SrItem star4() {
        return item(STAR_4_ITEM, 4L, "TRA", SERIES_ID);
    }

    private static SrItem series2Star2() {
        return item(SERIES_2_STAR_2_ITEM, 2L, "TRA", SERIES_2_ID);
    }

    private static SrItem series2Star3() {
        return item(SERIES_2_STAR_3_ITEM, 3L, "TRA", SERIES_2_ID);
    }

    private static SrItem series2Star4() {
        return item(SERIES_2_STAR_4_ITEM, 4L, "TRA", SERIES_2_ID);
    }

    private static SrItem plainItem() {
        return item(PLAIN_ITEM, 4L, null, null);
    }

    private static SrItem item(long id, Long starLevel, String ascensionType, Long seriesId) {
        SrItem item = new SrItem();
        item.setId(id);
        item.setStarLevel(starLevel);
        item.setAscensionType(ascensionType);
        item.setSeriesId(seriesId);
        item.setImage("img-" + id);
        return item;
    }

    private static Map<Long, SrItem> itemMap(SrItem... items) {
        return Arrays.stream(items).collect(Collectors.toMap(SrItem::getId, Function.identity()));
    }

    /**
     * MaterialRequirementView 测试桩
     */
    private static final class TestMaterial implements MaterialRequirementView {

        private final long itemId;
        private final long requiredQuantity;
        private final long ownedQuantity;

        private TestMaterial(long itemId, long requiredQuantity, long ownedQuantity) {
            this.itemId = itemId;
            this.requiredQuantity = requiredQuantity;
            this.ownedQuantity = ownedQuantity;
        }

        @Override
        public Long getItemId() {
            return itemId;
        }

        @Override
        public Long getRequiredQuantity() {
            return requiredQuantity;
        }

        @Override
        public Long getOwnedQuantity() {
            return ownedQuantity;
        }
    }
}
