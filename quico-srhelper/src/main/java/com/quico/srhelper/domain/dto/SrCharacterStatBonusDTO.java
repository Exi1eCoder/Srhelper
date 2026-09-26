package com.quico.srhelper.domain.dto;

import lombok.Data;

import java.util.List;

// 额外属性DTO
@Data
public class SrCharacterStatBonusDTO {
    private Long id;
    private Long characterId;           // 角色ID
    private Integer statIndex;          // 属性序号(1-10)
    private List<MaterialItemDTO> materials;
}