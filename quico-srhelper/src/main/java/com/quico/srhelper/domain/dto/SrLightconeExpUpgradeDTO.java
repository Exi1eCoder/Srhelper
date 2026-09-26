package com.quico.srhelper.domain.dto;

import lombok.Data;

/**
 * 光锥经验升级DTO
 */
@Data
public class SrLightconeExpUpgradeDTO {

    private Long id;

    private Integer starLevel;

    private Integer minLevel;

    private Integer maxLevel;

    private Long expRequired;

    private Long creditsRequired;
}
