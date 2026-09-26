package com.quico.srhelper.domain.vo;

import lombok.Data;

/**
 * 光锥经验升级VO
 */
@Data
public class SrLightconeExpUpgradeVO {

    private Long id;

    private Integer starLevel;

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
