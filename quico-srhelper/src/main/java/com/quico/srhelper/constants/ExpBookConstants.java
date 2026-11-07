package com.quico.srhelper.constants;

import java.util.Map;

/**
 * 经验书常量
 *
 * 角色经验书：37（2星，1000经验）、38（3星，5000经验）、39（4星，20000经验）
 * 光锥经验书：102（2星，500经验）、103（3星，2000经验）、104（4星，6000经验）
 *
 * @author quico
 */
public final class ExpBookConstants
{
    private ExpBookConstants()
    {
    }

    /** 角色2星经验书物品ID */
    public static final long CHARACTER_2_STAR_ITEM_ID = 37L;

    /** 角色3星经验书物品ID */
    public static final long CHARACTER_3_STAR_ITEM_ID = 38L;

    /** 角色4星经验书物品ID */
    public static final long CHARACTER_4_STAR_ITEM_ID = 39L;

    /** 角色2星经验书经验值 */
    public static final long CHARACTER_2_STAR_EXP = 1000L;

    /** 角色3星经验书经验值 */
    public static final long CHARACTER_3_STAR_EXP = 5000L;

    /** 角色4星经验书经验值 */
    public static final long CHARACTER_4_STAR_EXP = 20000L;

    /** 光锥2星经验书物品ID */
    public static final long LIGHT_CONE_2_STAR_ITEM_ID = 102L;

    /** 光锥3星经验书物品ID */
    public static final long LIGHT_CONE_3_STAR_ITEM_ID = 103L;

    /** 光锥4星经验书物品ID */
    public static final long LIGHT_CONE_4_STAR_ITEM_ID = 104L;

    /** 光锥2星经验书经验值 */
    public static final long LIGHT_CONE_2_STAR_EXP = 500L;

    /** 光锥3星经验书经验值 */
    public static final long LIGHT_CONE_3_STAR_EXP = 2000L;

    /** 光锥4星经验书经验值 */
    public static final long LIGHT_CONE_4_STAR_EXP = 6000L;

    /** 经验书缺口标记 */
    public static final String ITEM_TYPE_EXP_BOOK = "EXP_BOOK";

    /**
     * 计算用户持有的角色经验书总经验
     */
    public static long characterOwnedExp(Map<Long, Long> userItemMap)
    {
        return userItemMap.getOrDefault(CHARACTER_2_STAR_ITEM_ID, 0L) * CHARACTER_2_STAR_EXP
                + userItemMap.getOrDefault(CHARACTER_3_STAR_ITEM_ID, 0L) * CHARACTER_3_STAR_EXP
                + userItemMap.getOrDefault(CHARACTER_4_STAR_ITEM_ID, 0L) * CHARACTER_4_STAR_EXP;
    }

    /**
     * 计算用户持有的光锥经验书总经验
     */
    public static long lightConeOwnedExp(Map<Long, Long> userItemMap)
    {
        return userItemMap.getOrDefault(LIGHT_CONE_2_STAR_ITEM_ID, 0L) * LIGHT_CONE_2_STAR_EXP
                + userItemMap.getOrDefault(LIGHT_CONE_3_STAR_ITEM_ID, 0L) * LIGHT_CONE_3_STAR_EXP
                + userItemMap.getOrDefault(LIGHT_CONE_4_STAR_ITEM_ID, 0L) * LIGHT_CONE_4_STAR_EXP;
    }

    /**
     * 计算经验缺口折算成指定经验书的本数（向上取整）
     */
    public static long shortageBookCount(long requiredExp, long ownedExp, long bookExp)
    {
        long shortageExp = requiredExp - ownedExp;
        if (shortageExp <= 0L)
        {
            return 0L;
        }
        return (shortageExp + bookExp - 1L) / bookExp;
    }
}
