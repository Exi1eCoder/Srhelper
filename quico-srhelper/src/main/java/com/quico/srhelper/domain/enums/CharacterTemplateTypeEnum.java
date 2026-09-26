package com.quico.srhelper.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色晋升模板类型枚举
 * 用于标识角色属于哪种晋升材料消耗模板
 */
@Getter
@AllArgsConstructor
public enum CharacterTemplateTypeEnum {
    
    /** 四星角色（非记忆、欢愉） */
    FOUR_STAR_NORMAL("FOUR_STAR_NORMAL", "四星角色", false, false),
    
    /** 五星角色（非记忆、欢愉） */
    FIVE_STAR_NORMAL("FIVE_STAR_NORMAL", "五星角色", false, false),
    
    /** 记忆命途角色（多出忆灵技能、忆灵天赋） */
    MEMOSPRITE("MEMOSPRITE", "记忆角色", true, false),
    
    /** 欢愉命途角色（多出欢愉技能） */
    ELATION("ELATION", "欢愉角色", false, true);
    
    /** 代码值（存储在数据库中的值） */
    private final String code;
    
    /** 描述 */
    private final String desc;
    
    /** 是否为记忆命途角色 */
    private final boolean isMemosprite;
    
    /** 是否为欢愉命途角色 */
    private final boolean isElation;
    
    /**
     * 根据代码获取枚举
     */
    public static CharacterTemplateTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CharacterTemplateTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 根据角色命途判断模板类型
     * @param path 角色命途
     * @param starLevel 角色星级
     * @return 模板类型
     */
    public static CharacterTemplateTypeEnum fromPathAndStar(String path, Long starLevel) {
        if ("记忆".equals(path)) {
            return MEMOSPRITE;
        } else if ("欢愉".equals(path)) {
            return ELATION;
        } else if (starLevel != null && starLevel == 4) {
            return FOUR_STAR_NORMAL;
        } else {
            return FIVE_STAR_NORMAL;
        }
    }
}