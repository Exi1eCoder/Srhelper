package com.quico.srhelper.domain.enums;

/**
 * 物品晋升素材类型枚举
 * 用于标识物品属于哪类晋升素材
 */
public enum ItemAscensionTypeEnum {
    
    /** 世界掉落 */
    TRA("TRA", "世界掉落"),
    
    /** 拟造花萼 */
    CAL("CAL", "拟造花萼"),
    
    /** 凝滞虚影 */
    SS("SS", "凝滞虚影"),
    
    /** 历战余响 */
    EOW("EOW", "历战余响"),
    
    /** 命运的足迹 */
    TOD("TOD", "命运的足迹");
    
    private final String code;
    private final String desc;
    
    ItemAscensionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据代码获取枚举值
     */
    public static ItemAscensionTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ItemAscensionTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为可合成的基础材料类型（TRA/CAL）
     */
    public boolean isCombinable() {
        return this == TRA || this == CAL;
    }
}