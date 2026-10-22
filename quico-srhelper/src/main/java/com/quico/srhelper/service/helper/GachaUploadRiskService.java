package com.quico.srhelper.service.helper;

import java.util.concurrent.TimeUnit;

import com.quico.common.constant.HttpStatus;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.exception.BusinessException;
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
 * - 用户失败次数分级：<=3 提示格式异常，4 次触发须知弹窗，>=5 次封禁 1 天
 * - IP 失败次数 >= 20 封禁 1 天
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
     * 先落审计日志，再按信用分级决定返回体：
     * - fails <= 3：40010 数据格式异常
     * - fails == 4：40013 触发前端用户须知弹窗
     * - fails >= 5：封禁用户 1 天并抛出 500（伪装网络超时）
     *
     * @param e 校验异常（携带真实死因）
     * @param userId 当前登录用户ID
     * @param ip 客户端IP
     * @param file 上传文件（可为 null）
     * @return 40010 / 40013 的返回体
     */
    public AjaxResult handleViolation(UploadValidateException e, Long userId, String ip, MultipartFile file)
    {
        saveViolationLog(e, userId, ip, file);

        long userFails = incrementFail(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_USER_PREFIX + userId);
        long ipFails = incrementFail(SrhelperCacheConstants.GACHA_UPLOAD_FAIL_IP_PREFIX + ip);

        if (ipFails >= IP_BAN_FAILS)
        {
            ban(SrhelperCacheConstants.GACHA_UPLOAD_BAN_IP_PREFIX + ip);
            log.warn("IP 上传失败次数达到 {} 次，已封禁 1 天，ip={}", ipFails, ip);
        }

        if (userFails >= USER_BAN_FAILS)
        {
            ban(SrhelperCacheConstants.GACHA_UPLOAD_BAN_USER_PREFIX + userId);
            log.warn("用户上传失败次数达到 {} 次，已封禁 1 天，userId={}, ip={}, reason={}",
                    userFails, userId, ip, e.getViolationType());
            // 伪装成网络超时，不暴露封禁
            throw new BusinessException(HttpStatus.ERROR, MSG_NETWORK_TIMEOUT);
        }

        if (userFails == USER_NOTICE_FAILS)
        {
            return AjaxResult.error(CODE_USER_NOTICE, MSG_USER_NOTICE);
        }

        return AjaxResult.error(CODE_DATA_FORMAT_ERROR, MSG_DATA_FORMAT_ERROR);
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
     * 失败计数 +1，首次计数设置 24 小时过期
     */
    private long incrementFail(String key)
    {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L)
        {
            redisTemplate.expire(key, SrhelperCacheConstants.GACHA_UPLOAD_RISK_TTL_HOURS, TimeUnit.HOURS);
        }
        return count == null ? 0L : count;
    }

    /**
     * 设置 24 小时封禁标记
     */
    private void ban(String key)
    {
        redisTemplate.opsForValue().set(key, "1",
                SrhelperCacheConstants.GACHA_UPLOAD_RISK_TTL_HOURS, TimeUnit.HOURS);
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
