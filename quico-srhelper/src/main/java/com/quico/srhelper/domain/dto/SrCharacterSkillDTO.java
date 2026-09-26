package com.quico.srhelper.domain.dto;

import lombok.Data;

import java.util.List;

// 技能DTO
@Data
public class SrCharacterSkillDTO {
    private Long id;
    private Long characterId;           // 角色ID
    private String skillType;           // 技能类型(basic_atk/skill/ultimate/talent)
    private Integer targetLevel;        // 目标等级
    private List<MaterialItemDTO> materials;
}
