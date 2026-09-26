package com.quico.srhelper.domain.dto;

import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import lombok.Data;

import java.util.List;

// SrCharacterSaveDTO.java - 保存/更新请求
@Data
public class SrCharacterSaveDTO {

    /** 更新类型：simple=仅基本信息，full=完整保存（含材料绑定和晋升材料生成） */
    private String changeType;

    // 角色基本信息
    private SrCharacter character;

    // 材料绑定列表（4个基础材料）
    private List<SrCharacterMaterialBind> materialBinds;

    // 晋升材料列表
    private List<SrCharacterAscensionDTO> ascensions;

    // 技能列表
    private List<SrCharacterSkillDTO> skills;

    // 额外能力列表
    private List<SrCharacterBonusDTO> bonusAbilities;

    // 额外属性列表
    private List<SrCharacterStatBonusDTO> statBonuses;
}

