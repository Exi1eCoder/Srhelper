package com.quico.srhelper.config;

import com.quico.common.constant.CacheConstants;

/**
 * srhelper 模块专用缓存常量
 * Key 前缀放在此处而非 quico-common，避免去掉模块后 common 残留无关代码
 * TTL 仍引用 CacheConstants 统一管理
 */
public class SrhelperCacheConstants {

    public static final String PREFIX = "srhelper:";

    /** 角色列表缓存 */
    public static final String CHAR_LIST_KEY = PREFIX + "character_list:";

    /** 光锥列表缓存 */
    public static final String LIGHTCONE_LIST_KEY = PREFIX + "lightcone_list:";

    /** 物品列表缓存 */
    public static final String ITEM_LIST_KEY = PREFIX + "item_list:";

    /** 角色经验升级配置缓存 */
    public static final String CHAR_EXP_UPGRADE_KEY = PREFIX + "exp_upgrade:";

    /** 光锥经验升级配置缓存 */
    public static final String LIGHTCONE_EXP_UPGRADE_KEY = PREFIX + "lc_exp_upgrade:";

    /** 养成计算缓存 */
    public static final String CULTIVATION_KEY = PREFIX + "sr:cultivation:";

    /** 卡池项目图片URL缓存（本地 Caffeine） */
    public static final String GACHA_ITEM_IMAGE_KEY = PREFIX + "gacha_item_image:";

    /** 抽卡统计重算：待重算 uid 集合（Set） */
    public static final String GACHA_RECALC_PENDING_KEY = PREFIX + "gacha:recalc:pending";

    /** 抽卡统计重算：重算中 uid（ZSet，score=进入时间戳，用于卡死检测） */
    public static final String GACHA_RECALC_RUNNING_KEY = PREFIX + "gacha:recalc:running";

    /** 抽卡统计重算：完成标记前缀（String，{uid} 拼尾，10 分钟过期） */
    public static final String GACHA_RECALC_DONE_PREFIX = PREFIX + "gacha:recalc:done:";

    /** 抽卡统计重算：分布式锁前缀（{uid} 拼尾） */
    public static final String GACHA_RECALC_LOCK_PREFIX = PREFIX + "gacha:recalc:lock:";

    /** 抽卡统计重算：失败重试次数前缀（{uid} 拼尾） */
    public static final String GACHA_RECALC_RETRY_PREFIX = PREFIX + "gacha:recalc:retry:";

    /** 重算完成标记过期时间（分钟，24 小时） */
    public static final long GACHA_RECALC_DONE_TTL_MINUTES = 1440;

    /** 手动重算当天限流前缀（{uid} 拼尾，当天 24:00 过期） */
    public static final String GACHA_RECALC_MANUAL_PREFIX = PREFIX + "gacha:recalc:manual:";

    /** 配置类缓存 TTL（代理到 CacheConstants） */
    public static final long TTL_CONFIG = CacheConstants.TTL_CONFIG;

    /** 计算类缓存 TTL（代理到 CacheConstants） */
    public static final long TTL_CALC = CacheConstants.TTL_CALC;
}
