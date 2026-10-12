package com.quico.srhelper.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 养成材料排序枚举
 * order 越小越靠前，用于材料返回给前端前的统一排序
 * 名称与 sr_item.ascension_type 的字典值保持一致（TOD/EOW/SS/TRA/CAL）；
 * 信用点、旅行见闻的 ascension_type 为空，需按物品单独识别
 */
@Getter
@AllArgsConstructor
public enum MaterialSortEnum {

    /** 信用点 */
    CREDIT(1, "信用点"),

    /** 旅行见闻（角色升级经验书） */
    EXP_BOOK(2, "旅行见闻"),

    /** 命运的足迹 */
    TOD(3, "命运的足迹"),

    /** 历战余响材料 */
    EOW(4, "历战余响"),

    /** 凝滞虚影材料 */
    SS(5, "凝滞虚影"),

    /** 世界掉落材料 */
    TRA(6, "世界掉落"),

    /** 拟造花萼材料 */
    CAL(7, "拟造花萼"),

    /** 其他（未识别类型，排最后） */
    UNKNOWN(99, "其他");

    private final int order;
    private final String desc;

    /**
     * 按 sr_item.ascension_type 的字典值解析排序类型
     */
    public static MaterialSortEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (MaterialSortEnum type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}