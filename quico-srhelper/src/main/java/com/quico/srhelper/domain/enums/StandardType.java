package com.quico.srhelper.domain.enums;

/**
 * 角色常驻类型
 * 用于抽卡分析判定“歪”：抽到常驻/星缘相遇角色视为歪
 *
 * @author quico
 */
public enum StandardType {

    /** 限定角色，不进常驻池 */
    LIMITED(0, "限定"),

    /** 基础常驻，开服即在常驻池 */
    STANDARD_BASE(1, "基础常驻"),

    /** 星缘相遇，后续版本加入常驻池 */
    STANDARD_CELESTIAL(2, "星缘相遇");

    /** 数据库存储值 */
    private final int code;

    /** 描述 */
    private final String desc;

    StandardType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 按数据库存储值转换为枚举，未知值返回 null
     */
    public static StandardType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StandardType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
