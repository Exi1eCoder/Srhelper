package com.quico.srhelper.domain.enums;

/**
 * 角色晋升材料类型枚举
 * code 值与数据库 sr_character_ascension_material 表中的 ascension_type 字段值保持一致
 */
public enum AscensionTypeEnum {
    
    /** 突破阶段材料 */
    LEVEL("level", "突破"),
    
    /** 普攻 */
    BASIC_ATK("basicatk", "普攻"),
    
    /** 战技 */
    SKILL("skill", "战技"),
    
    /** 终结技 */
    ULTIMATE("ultimate", "终结技"),
    
    /** 天赋 */
    TALENT("talent", "天赋"),
    
    /** 秘技 */
    TECHNIQUE("technique", "秘技"),
    
    /** 欢愉技 */
    ELATION_SKILL("elationskill", "欢愉技"),
    
    /** 额外能力 */
    BONUS_ABILITY("bonusability", "额外能力"),
    
    /** 额外属性 */
    STAT_BONUS("statbonus", "额外属性");
    
    private final String code;
    private final String name;
    
    AscensionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据code获取枚举
     */
    public static AscensionTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AscensionTypeEnum type : AscensionTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 判断是否匹配
     */
    public boolean matches(String code) {
        return this.code.equals(code);
    }
}