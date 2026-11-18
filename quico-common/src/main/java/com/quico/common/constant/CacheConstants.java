package com.quico.common.constant;

/**
 * 缓存的key 常量
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 邮箱验证码 redis key（按用户 + 目标邮箱隔离）
     */
    public static final String EMAIL_CODE_KEY = "email_code:";

    /**
     * 邮箱验证码错误次数 redis key
     */
    public static final String EMAIL_CODE_ERROR_KEY = "email_code_error:";

    /**
     * 邮箱验证码发送间隔限制 redis key
     */
    public static final String EMAIL_CODE_LIMIT_KEY = "email_code_limit:";

    /**
     * 邮箱验证码每日发送量 redis key
     */
    public static final String EMAIL_CODE_DAILY_KEY = "email_code_daily:";

    /**
     * 手机验证码 redis key（按用户 + 目标手机号隔离）
     */
    public static final String PHONE_CODE_KEY = "phone_code:";

    /**
     * 手机验证码错误次数 redis key
     */
    public static final String PHONE_CODE_ERROR_KEY = "phone_code_error:";

    /**
     * 手机验证码发送间隔限制 redis key
     */
    public static final String PHONE_CODE_LIMIT_KEY = "phone_code_limit:";

    /**
     * 手机验证码每日发送量 redis key
     */
    public static final String PHONE_CODE_DAILY_KEY = "phone_code_daily:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    // ======================== 缓存 TTL（通用，各模块共用） ========================

    /** 配置类数据缓存（基本不变）：7天（单位：分钟） */
    public static final long TTL_CONFIG = 7 * 24 * 60;

    /** 计算类结果缓存（可能变化）：30分钟（单位：分钟） */
    public static final long TTL_CALC = 30;
}
