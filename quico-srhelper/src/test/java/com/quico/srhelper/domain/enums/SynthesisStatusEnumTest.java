package com.quico.srhelper.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SynthesisStatusEnum.withFarmRequired() 单元测试
 *
 * 锁定"经验书等无法合成、只能刷取的需求"追加到整体状态时的合并规则：
 * - ENOUGH + 刷取 → NEED_FARM
 * - ENOUGH_BY_SYNTHESIS + 刷取 → PARTIAL_SYNTHESIS
 * - PARTIAL_SYNTHESIS + 刷取 → PARTIAL_SYNTHESIS
 * - NEED_FARM + 刷取 → NEED_FARM
 */
@DisplayName("刷取需求合并整体状态规则")
class SynthesisStatusEnumTest {

    @Test
    @DisplayName("ENOUGH + 必须刷取 → NEED_FARM")
    void shouldTurnEnoughIntoNeedFarm() {
        assertEquals(SynthesisStatusEnum.NEED_FARM, SynthesisStatusEnum.ENOUGH.withFarmRequired());
    }

    @Test
    @DisplayName("ENOUGH_BY_SYNTHESIS + 必须刷取 → PARTIAL_SYNTHESIS")
    void shouldTurnEnoughBySynthesisIntoPartialSynthesis() {
        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS,
                SynthesisStatusEnum.ENOUGH_BY_SYNTHESIS.withFarmRequired());
    }

    @Test
    @DisplayName("PARTIAL_SYNTHESIS + 必须刷取 → PARTIAL_SYNTHESIS（保持不变）")
    void shouldKeepPartialSynthesis() {
        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS,
                SynthesisStatusEnum.PARTIAL_SYNTHESIS.withFarmRequired());
    }

    @Test
    @DisplayName("NEED_FARM + 必须刷取 → NEED_FARM（保持不变）")
    void shouldKeepNeedFarm() {
        assertEquals(SynthesisStatusEnum.NEED_FARM, SynthesisStatusEnum.NEED_FARM.withFarmRequired());
    }
}
