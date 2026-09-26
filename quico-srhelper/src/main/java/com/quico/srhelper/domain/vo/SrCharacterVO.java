package com.quico.srhelper.domain.vo;

import lombok.Data;

// 角色基本信息VO
@Data
public class SrCharacterVO {
    private Long id;
    private String characterName;
    private String characterIcon;
    private Integer rarity;
    private String path;
    private String element;
    private Integer maxLevel;
    private String description;
}
