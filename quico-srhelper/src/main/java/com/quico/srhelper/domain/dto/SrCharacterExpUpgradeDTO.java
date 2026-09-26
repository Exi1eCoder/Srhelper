package com.quico.srhelper.domain.dto;

import lombok.Data;

/**
 * 角色经验升级DTO
 */
@Data
public class SrCharacterExpUpgradeDTO {
    
    private Long id;
    
    private Integer minLevel;
    
    private Integer maxLevel;
    
    private Long expRequired;
    
    private Long creditsRequired;
}