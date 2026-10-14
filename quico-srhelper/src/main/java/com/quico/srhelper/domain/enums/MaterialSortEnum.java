package com.quico.srhelper.domain.enums;

import com.quico.srhelper.domain.SrItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 养成材料排序枚举
 * order 越小越靠前，用于材料返回给前端前的统一排序
 * 名称与 sr_item.ascension_type 的字典值保持一致（TOD/EOW/SS/TRA/CAL）；
 * 信用点、经验书的 ascension_type 为空，需按物品单独识别
 */
@Getter
@AllArgsConstructor
public enum MaterialSortEnum {

    /** 信用点 */
    CREDIT(1, "信用点"),

    /** 经验书（角色升级书"旅行见闻"、光锥升级书"稀薄以太"，ascension_type 均为空） */
    EXP_BOOK(2, "经验书"),

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

    /** 经验书物品名集合（ascension_type 为空，按名称识别） */
    private static final Set<String> EXP_BOOK_NAMES =
            new HashSet<>(Arrays.asList("旅行见闻", "稀薄以太"));

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

    /**
     * 解析材料的类型排序优先级（角色、光锥养成计算共用）
     * 信用点、经验书的 ascension_type 为空，按物品单独识别；其余按 ascension_type 字典值
     */
    public static int resolveOrder(SrItem item) {
        if (item == null) {
            return UNKNOWN.getOrder();
        }
        if (item.getId() != null && item.getId().equals(CommonItemEnum.CREDIT.getItemId())) {
            return CREDIT.getOrder();
        }
        if (EXP_BOOK_NAMES.contains(item.getItemName())) {
            return EXP_BOOK.getOrder();
        }
        MaterialSortEnum type = fromCode(item.getAscensionType());
        return type != null ? type.getOrder() : UNKNOWN.getOrder();
    }
}