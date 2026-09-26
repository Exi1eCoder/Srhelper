package com.quico.srhelper.config;

import com.quico.common.constant.CacheConstants;

/**
 * srhelper 模块专用缓存常量
 * Key 前缀放在此处而非 quico-common，避免去掉模块后 common 残留无关代码
 * TTL 仍引用 CacheConstants 统一管理
 */
public class SrhelperCacheConstants {

    /** 角色经验升级配置缓存 */
    public static final String CHAR_EXP_UPGRADE_KEY = "exp_upgrade:";

    /** 光锥经验升级配置缓存 */
    public static final String LIGHTCONE_EXP_UPGRADE_KEY = "lc_exp_upgrade:";

    /** 养成计算缓存 */
    public static final String CULTIVATION_KEY = "sr:cultivation:";

    /** 卡池项目图片URL缓存（本地 Caffeine） */
    public static final String GACHA_ITEM_IMAGE_KEY = "gacha_item_image:";

    /** 配置类缓存 TTL（代理到 CacheConstants） */
    public static final long TTL_CONFIG = CacheConstants.TTL_CONFIG;

    /** 计算类缓存 TTL（代理到 CacheConstants） */
    public static final long TTL_CALC = CacheConstants.TTL_CALC;
}
