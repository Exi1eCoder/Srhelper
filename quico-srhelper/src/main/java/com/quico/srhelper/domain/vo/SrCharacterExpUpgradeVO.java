package com.quico.srhelper.domain.vo;

import lombok.Data;

/**
 * 角色经验升级VO
 */
@Data
public class SrCharacterExpUpgradeVO {
    
    private Long id;
    
    private Integer minLevel;
    
    private Integer maxLevel;
    
    private Long expRequired;
    
    private Long creditsRequired;
    
    private Long expItemId;
    private String expItemName;
    private String expItemImage;
    private Long creditsItemId;
    private String creditsItemName;
    private String creditsItemImage;
    
    public String getLevelRange() {
        return minLevel + " → " + maxLevel;
    }
}