package com.quico.srhelper.domain.dto;

import lombok.Data;

import java.util.List;

// 额外能力DTO
@Data
public class SrCharacterBonusDTO {
    private Long id;
    private Long characterId;           // 角色ID
    private Integer abilityIndex;       // 能力序号(1-3)
    private List<MaterialItemDTO> materials;
}
