package com.quico.srhelper.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quico.srhelper.constants.ExpBookConstants;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrUserCharacter;
import com.quico.srhelper.domain.enums.SynthesisStatusEnum;
import com.quico.srhelper.domain.vo.MaterialCalculationResultVO;
import com.quico.srhelper.domain.vo.SrCharacterCultivationVO;
import com.quico.srhelper.domain.vo.SrCharacterExpUpgradeVO;
import com.quico.srhelper.mapper.SrCharacterAscensionMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterBonusAbilityMaterialMapper;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrCharacterStatBonusMaterialMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrUserCharacterMapper;
import com.quico.srhelper.mapper.SrUserItemMapper;
import com.quico.srhelper.service.ISrCharacterExpUpgradeService;
import com.quico.srhelper.service.helper.MaterialSynthesisCalculator;
import com.quico.srhelper.service.helper.UsableChecker;

/**
 * 经验书缺口对整体状态的影响单元测试
 *
 * 经验书无法合成、只能刷取，追加后必须走 SynthesisStatusEnum.withFarmRequired()：
 * - 计算器返回 ENOUGH_BY_SYNTHESIS + 经验书缺口 → PARTIAL_SYNTHESIS（不得回退为 NEED_FARM）
 * - 计算器返回 PARTIAL_SYNTHESIS + 经验书缺口 → PARTIAL_SYNTHESIS
 * - 计算器返回 ENOUGH + 经验书缺口 → NEED_FARM
 *
 * 若有人把 appendExpBookRequirement 里的合并逻辑改回无条件 NEED_FARM，本类会失败。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("经验书缺口合并整体状态")
class SrCultivationServiceImplTest {

    private static final long USER_ID = 1L;
    private static final long USER_CHARACTER_ID = 78L;
    private static final long CHARACTER_ID = 155L;

    /** 1→80 级所需总经验；2星经验书 1000 经验/本，缺口 2 本 */
    private static final long REQUIRED_EXP = 2000L;

    @Mock
    private SrUserCharacterMapper userCharacterMapper;

    @Mock
    private SrCharacterMapper characterMapper;

    @Mock
    private SrCharacterAscensionMaterialMapper ascensionMaterialMapper;

    @Mock
    private SrCharacterBonusAbilityMaterialMapper bonusAbilityMaterialMapper;

    @Mock
    private SrCharacterStatBonusMaterialMapper statBonusMaterialMapper;

    @Mock
    private SrItemMapper itemMapper;

    @Mock
    private SrUserItemMapper userItemMapper;

    @Mock
    private ISrCharacterExpUpgradeService expUpgradeService;

    @Mock
    private UsableChecker usableChecker;

    @Mock
    private MaterialSynthesisCalculator synthesisCalculator;

    @InjectMocks
    private SrCultivationServiceImpl service;

    @Test
    @DisplayName("计算器 ENOUGH_BY_SYNTHESIS + 经验书缺口 → PARTIAL_SYNTHESIS（不得变为 NEED_FARM）")
    void shouldTurnEnoughBySynthesisIntoPartialSynthesisWhenExpBooksShort() {
        SrCharacterCultivationVO result = calculate(SynthesisStatusEnum.ENOUGH_BY_SYNTHESIS);

        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS, result.getCalculation().getStatus());
        assertExpBookRequirement(result.getCalculation(), 2L);
    }

    @Test
    @DisplayName("计算器 PARTIAL_SYNTHESIS + 经验书缺口 → PARTIAL_SYNTHESIS（保持不变）")
    void shouldKeepPartialSynthesisWhenExpBooksShort() {
        SrCharacterCultivationVO result = calculate(SynthesisStatusEnum.PARTIAL_SYNTHESIS);

        assertEquals(SynthesisStatusEnum.PARTIAL_SYNTHESIS, result.getCalculation().getStatus());
        assertExpBookRequirement(result.getCalculation(), 2L);
    }

    @Test
    @DisplayName("计算器 ENOUGH + 经验书缺口 → NEED_FARM")
    void shouldTurnEnoughIntoNeedFarmWhenExpBooksShort() {
        SrCharacterCultivationVO result = calculate(SynthesisStatusEnum.ENOUGH);

        assertEquals(SynthesisStatusEnum.NEED_FARM, result.getCalculation().getStatus());
        assertExpBookRequirement(result.getCalculation(), 2L);
    }

    /**
     * 构造"无材料配置、仅有 1→80 经验需求、背包为空"的角色，让计算器状态由桩指定，
     * 从而只验证经验书缺口对整体状态的影响。
     */
    private SrCharacterCultivationVO calculate(SynthesisStatusEnum calculatorStatus) {
        SrUserCharacter uc = new SrUserCharacter();
        uc.setId(USER_CHARACTER_ID);
        uc.setUserId(USER_ID);
        uc.setCharacterId(CHARACTER_ID);
        uc.setLevel(1L);
        when(userCharacterMapper.selectSrUserCharacterById(USER_CHARACTER_ID)).thenReturn(uc);

        SrCharacter character = new SrCharacter();
        character.setId(CHARACTER_ID);
        character.setCharacterName("真珠");
        character.setAvatar("avatar");
        when(characterMapper.selectSrCharacterById(CHARACTER_ID)).thenReturn(character);

        // 无角色材料配置
        when(ascensionMaterialMapper.selectByCharacterId(CHARACTER_ID)).thenReturn(Collections.emptyList());
        when(ascensionMaterialMapper.selectList(any())).thenReturn(Collections.emptyList());

        // 1→80 级共需 2000 经验，不产生信用点材料
        SrCharacterExpUpgradeVO upgrade = new SrCharacterExpUpgradeVO();
        upgrade.setMinLevel(1);
        upgrade.setMaxLevel(80);
        upgrade.setExpRequired(REQUIRED_EXP);
        when(expUpgradeService.selectAllExpUpgrades()).thenReturn(List.of(upgrade));

        // 背包为空：无经验书，也无低阶材料富余
        when(userItemMapper.selectSrUserItemList(any())).thenReturn(Collections.emptyList());

        // 计算器状态由桩指定，隔离材料折算逻辑，只验证经验书合并
        MaterialCalculationResultVO calculation = new MaterialCalculationResultVO();
        calculation.setStatus(calculatorStatus);
        calculation.setRequired(new ArrayList<>());
        when(synthesisCalculator.calculate(any(), any(), any())).thenReturn(calculation);

        SrItem expBook = new SrItem();
        expBook.setId(ExpBookConstants.CHARACTER_2_STAR_ITEM_ID);
        expBook.setImage("exp-book");
        when(itemMapper.selectSrItemById(ExpBookConstants.CHARACTER_2_STAR_ITEM_ID)).thenReturn(expBook);

        return service.calculateCharacterCultivation(USER_CHARACTER_ID, 80);
    }

    private void assertExpBookRequirement(MaterialCalculationResultVO calculation, long expectedCount) {
        assertNotNull(calculation.getRequired());
        MaterialCalculationResultVO.RequiredItem expBookRequired = calculation.getRequired().stream()
                .filter(item -> ExpBookConstants.ITEM_TYPE_EXP_BOOK.equals(item.getItemType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未生成经验书缺口"));
        assertEquals(expectedCount, expBookRequired.getDifference().longValue());
    }
}
