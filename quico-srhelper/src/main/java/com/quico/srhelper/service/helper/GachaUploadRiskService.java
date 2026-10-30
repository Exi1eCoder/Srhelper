package com.quico.srhelper.service.helper;

import java.util.concurrent.TimeUnit;

import com.quico.common.constant.CacheConstants;
import com.quico.common.constant.HttpStatus;
import com.quico.common.exception.ServiceException;
import com.quico.srhelper.constants.SrhelperCacheConstants;
import com.quico.srhelper.domain.SysUploadViolationLog;
import com.quico.srhelper.exception.UploadValidateException;
import com.quico.srhelper.mapper.SysUploadViolationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 抽卡记录上传风控服务（第五层）
 *
 * 统一封装 Redis 计数/封禁操作与审计落库：
 * - 真实死因写入 sys_upload_violation_log（前端永远看不到具体原因）
 * - 用户失败次数分级：<=3 提示格式异常，4 次触发须知弹窗，>=5 次封禁
 * - IP 失败次数 >= 20 封禁
 * - 计数与封禁有效期取系统参数 gacha.upload.risk.ttl.minutes（默认 1440 分钟）
 *
 * @author quico
 */
@Slf4j
@Service
public class GachaUploadRiskService
{
    /** 用户失败次数达到该值触发前端须知弹窗 */
    private static final long USER_NOTICE_FAILS = 4;

    /** 用户失败次数达到该值封禁 1 天 */
    private static final long USER_BAN_FAILS = 5;

    /** IP 失败次数达到该值封禁 1 天 */
    private static final long IP_BAN_FAILS = 20;

    /** error_msg 字段长度上限 */
    private static final int ERROR_MSG_MAX_LENGTH = 500;

    /** 风控计数/封禁有效期系统参数键（单位：分钟） */
    private static final String CONFIG_RISK_TTL_MINUTES = "gacha.upload.risk.ttl.minutes";

    /** 系统参数缺失或非法时的兜底有效期（24 小时） */
    private static final long DEFAULT_RISK_TTL_MINUTES = 24 * 60;

    /** 40010 数据格式异常 */
    public static final int CODE_DATA_FORMAT_ERROR = 40010;

    /** 40012 用户今日上传次数已达上限 */
    public static final int CODE_USER_BANNED = 40012;

    /** 40013 触发前端用户须知弹窗 */
    public static final int CODE_USER_NOTICE = 40013;

    /** 统一话术：格式异常（不暴露具体死因） */
    public static final String MSG_DATA_FORMAT_ERROR = "数据格式异常，请重新下载并上传游戏原始导出文件。";

    /** 统一话术：用户须知弹窗 */
    public static final String MSG_USER_NOTICE = "检测到多次文件异常，请确认上传的是游戏官方导出的原始跃迁记录。";

    /** 统一话术：网络超时（封禁时伪装） */
    public static final String MSG_NETWORK_TIMEOUT = "网络超时，请稍后重试。";

    /** 统一话术：IP 封禁 */
    public static final String MSG_IP_BANNED = "网络环境异常，请稍后重试。";

    /** 统一话术：用户封禁 */
    public static final String MSG_USER_BANNED = "今日上传次数已达上限，请明日再试。";

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private SysUploadViolationLogMapper uploadViolationLogMapper;

    /**
     * IP 是否被封禁
     */
    public boolean isIpBanned(String ip)
    {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SrhelperCacheConstants.GACHA_UPLOAD_BAN_IP_PREFIX + ip));
    }

    /**
     * 用户是否被封禁
     */
    public boolean isUserBanned(String userId)
    {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SrhelperCacheConstants.GACHA_UPLOAD_BAN_USER_PREFIX + userId));
    }

    /**
     * 处理一次上传校验失败
     *
     * 先落审计日志，再按信用分级决定抛出哪个业务异常（由全局异常处理器统一封装为 AjaxResult）：
     * - IP 失败次数 >= 20：封禁 IP 1 天并抛出 500（伪装网络超时）
     * - 用户失败次数 <= 3：40010 数据格式异常
     * - 用户失败次数 == 4：40013 触发前端用户须知弹窗
     * - 用户失败次数 >= 5：封禁用户 1 天并抛出 500（伪装网络超时）
     *
     * @param e 校验异常（携带真实死因）
     * @param userId 当前登录用户ID
     * @param ip 客户端IP
     * @param file 上传文件（可为 null）
     */
    public void handleViolationAsException(UploadValidateException e, Long userId, String ip, MultipartFile file)
    {
        saveViolationLog(e, userId, ip, file);

        long ttlMinutes = riskTtlMinutes();
        long userFails = incrementFail(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_USER_PREFIX + userId, ttlMinutes);
        long ipFails = incrementFail(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_IP_PREFIX + ip, ttlMinutes);

        if (ipFails >= IP_BAN_FAILS)
        {
            ban(SrhelperCacheConstants.GACHA_UPLOAD_BAN_IP_PREFIX + ip, ttlMinutes);
            log.warn("IP 上传失败次数达到 {} 次，已封禁 {} 分钟，ip={}", ipFails, ttlMinutes, ip);
        }

        if (userFails >= USER_BAN_FAILS)
        {
            ban(SrhelperCacheConstants.GACHA_UPLOAD_BAN_USER_PREFIX + userId, ttlMinutes);
            log.warn("用户上传失败次数达到 {} 次，已封禁 {} 分钟，userId={}, ip={}, reason={}",
                    userFails, ttlMinutes, userId, ip, e.getViolationType());
            // 伪装成网络超时，不暴露封禁
            throw new ServiceException(MSG_NETWORK_TIMEOUT, HttpStatus.ERROR);
        }

        if (userFails == USER_NOTICE_FAILS)
        {
            throw new ServiceException(MSG_USER_NOTICE, CODE_USER_NOTICE);
        }

        throw new ServiceException(MSG_DATA_FORMAT_ERROR, CODE_DATA_FORMAT_ERROR);
    }

    /**
     * 导入成功：清除用户与 IP 的失败计数
     */
    public void clearFailCount(String userId, String ip)
    {
        redisTemplate.delete(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_USER_PREFIX + userId);
        redisTemplate.delete(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_IP_PREFIX + ip);
    }

    /**
     * 失败计数 +1，首次计数按系统参数设置过期时间
     */
    private long incrementFail(String key, long ttlMinutes)
    {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L)
        {
            redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
        }
        return count == null ? 0L : count;
    }

    /**
     * 设置封禁标记，有效期取系统参数
     */
    private void ban(String key, long ttlMinutes)
    {
        redisTemplate.opsForValue().set(key, "1", ttlMinutes, TimeUnit.MINUTES);
    }

    /**
     * 读取风控计数/封禁有效期（分钟）
     * 由参数设置维护：sys_config:gacha.upload.risk.ttl.minutes
     * 读取失败或值非法时回退默认 24 小时，避免风控失效
     */
    private long riskTtlMinutes()
    {
        try
        {
            Object value = redisTemplate.opsForValue().get(CacheConstants.SYS_CONFIG_KEY + CONFIG_RISK_TTL_MINUTES);
            log.debug("当前系统参数 {} 值：{}分钟", CONFIG_RISK_TTL_MINUTES, value);
            if (value != null)
            {
                long minutes = Long.parseLong(value.toString().trim());
                if (minutes > 0)
                {
                    return minutes;
                }
            }
        }
        catch (Exception e)
        {
            log.warn("读取系统参数 {} 失败，使用默认 {} 分钟", CONFIG_RISK_TTL_MINUTES, DEFAULT_RISK_TTL_MINUTES, e);
        }
        return DEFAULT_RISK_TTL_MINUTES;
    }

    /**
     * 审计落库；落库失败不能影响风控主流程
     */
    private void saveViolationLog(UploadValidateException e, Long userId, String ip, MultipartFile file)
    {
        try
        {
            String fileName = file == null ? null : file.getOriginalFilename();
            if (fileName != null && fileName.length() > 255)
            {
                fileName = fileName.substring(0, 255);
            }
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > ERROR_MSG_MAX_LENGTH)
            {
                errorMsg = errorMsg.substring(0, ERROR_MSG_MAX_LENGTH);
            }

            SysUploadViolationLog violationLog = SysUploadViolationLog.builder()
                    .userId(userId)
                    .ipAddress(ip)
                    .fileName(fileName)
                    .fileSize(file == null ? null : file.getSize())
                    .violationType(e.getViolationType().name())
                    .isMalicious(e.getViolationType().getIsMalicious())
                    .errorMsg(errorMsg)
                    .build();
            uploadViolationLogMapper.insertSysUploadViolationLog(violationLog);
        }
        catch (Exception ex)
        {
            log.error("上传违规审计日志落库失败，userId={}, ip={}, type={}",
                    userId, ip, e.getViolationType(), ex);
        }
    }
}
