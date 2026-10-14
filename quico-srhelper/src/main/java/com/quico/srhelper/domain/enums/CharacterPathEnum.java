package com.quico.srhelper.domain.enums;

/**
 * 角色命途类型
 * 用于判断命途特有维度（忆灵技能/忆灵天赋/欢愉技）是否参与材料计算，
 * 以及保存时按命途校验取值范围、把非本命途维度重置为默认值
 *
 * maxXxx 表示该命途下对应维度的最大等级；非本命途维度上限固定为默认值DEFAULT_LEVEL（1），
 * 即"1 到 1 消耗为 0"，不会产生材料需求
 */
public enum CharacterPathEnum {

    /** 普通命途（毁灭/巡猎/智识/同谐/虚无/存护/丰饶等），不含命途特有维度 */
    NORMAL("普通", 1, 1, 1),

    /** 记忆命途：忆灵技能、忆灵天赋为该命途特有 */
    REMEMBRANCE("记忆", 6, 6, 1),

    /** 欢愉命途：欢愉技为该命途特有 */
    ELATION("欢愉", 1, 1, 10);

    /** 命途特有维度的默认等级（非本命途维度恒为该值） */
    public static final int DEFAULT_LEVEL = 1;

    private final String desc;
    private final int maxMemospriteskillLevel;
    private final int maxMemospritetalentLevel;
    private final int maxElationskillLevel;

    CharacterPathEnum(String desc, int maxMemospriteskillLevel, int maxMemospritetalentLevel,
                      int maxElationskillLevel) {
        this.desc = desc;
        this.maxMemospriteskillLevel = maxMemospriteskillLevel;
        this.maxMemospritetalentLevel = maxMemospritetalentLevel;
        this.maxElationskillLevel = maxElationskillLevel;
    }

    public String getDesc() {
        return desc;
    }

    public int getMaxMemospriteskillLevel() {
        return maxMemospriteskillLevel;
    }

    public int getMaxMemospritetalentLevel() {
        return maxMemospritetalentLevel;
    }

    public int getMaxElationskillLevel() {
        return maxElationskillLevel;
    }

    /**
     * 是否记忆命途（忆灵技能/忆灵天赋参与计算）
     */
    public boolean hasMemosprite() {
        return maxMemospriteskillLevel > DEFAULT_LEVEL || maxMemospritetalentLevel > DEFAULT_LEVEL;
    }

    /**
     * 是否欢愉命途（欢愉技参与计算）
     */
    public boolean hasElationSkill() {
        return maxElationskillLevel > DEFAULT_LEVEL;
    }

    /**
     * 根据角色命途名称判断命途类型
     * 兼容中英文命途名（sr_character.path，字典类型 sr_path）
     */
    public static CharacterPathEnum fromPath(String path) {
        if (path == null || path.isEmpty()) {
            return NORMAL;
        }
        String lowerPath = path.toLowerCase();
        if (lowerPath.contains("memory") || lowerPath.contains("remembrance") || lowerPath.contains("记忆")) {
            return REMEMBRANCE;
        }
        if (lowerPath.contains("elation") || lowerPath.contains("欢愉")) {
            return ELATION;
        }
        return NORMAL;
    }
}
