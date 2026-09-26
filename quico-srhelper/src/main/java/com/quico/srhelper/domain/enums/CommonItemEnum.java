package com.quico.srhelper.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用物品枚举（全局通用，不随角色变动）
 * 供角色晋升材料生成器和光锥材料生成器统一引用
 */
@Getter
@AllArgsConstructor
public enum CommonItemEnum {

    /** 信用点 */
    CREDIT(32L, "信用点"),

    /** 命运的足迹 */
    TOD(70L, "命运的足迹");

    private final Long itemId;
    private final String desc;
}
