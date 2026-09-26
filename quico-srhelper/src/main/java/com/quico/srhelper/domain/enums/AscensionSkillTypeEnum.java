package com.quico.srhelper.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色晋升技能类型枚举
 * 对应数据字典 sr_ascension_skill_type
 */
@Getter
@AllArgsConstructor
public enum AscensionSkillTypeEnum {

    /** 等级突破 */
    LEVEL("levelasc", "等级突破"),

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

    /** 额外能力 */
    BONUS_ABILITY("bonusability", "额外能力"),

    /** 额外属性 */
    STAT_BONUS("statbonus", "额外属性"),

    /** 忆灵技能 */
    MEMOSPRITE_SKILL("memospriteskill", "忆灵技能"),

    /** 忆灵天赋 */
    MEMOSPRITE_TALENT("memospritetalent", "忆灵天赋"),

    /** 欢愉技 */
    ELATION_SKILL("elationskill", "欢愉技");

    /**
     * 代码值（存储在数据库中的值）
     */
    private final String code;

    /**
     * 描述（对应数据字典的标签）
     */
    private final String desc;

    /**
     * 根据代码获取枚举
     */
    public static AscensionSkillTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AscensionSkillTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为技能类型（普攻/战技/终结技/天赋/忆灵技能/忆灵天赋/欢愉技）
     */
    public boolean isSkillType() {
        return this == BASIC_ATK || this == SKILL || this == ULTIMATE 
            || this == TALENT || this == MEMOSPRITE_SKILL 
            || this == MEMOSPRITE_TALENT || this == ELATION_SKILL;
    }

    /**
     * 判断是否为等级突破类型
     */
    public boolean isLevelType() {
        return this == LEVEL;
    }
}