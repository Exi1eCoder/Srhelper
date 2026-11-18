package com.quico.framework.web.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.quico.common.constant.CacheConstants;
import com.quico.common.constant.HttpStatus;
import com.quico.common.core.redis.RedisCache;
import com.quico.common.exception.BusinessException;
import com.quico.common.exception.ServiceException;
import com.quico.common.utils.StringUtils;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码服务
 *
 * 用于个人中心修改绑定邮箱前的身份校验：验证码发送到「新邮箱」，
 * 校验通过即证明当前用户可控制该邮箱。
 *
 * @author quico
 */
@Slf4j
@Service
public class EmailVerifyCodeService
{
    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /** 同一用户两次发送的最小间隔（秒） */
    private static final int SEND_INTERVAL_SECONDS = 60;

    /** 单个验证码允许的最大错误次数，超过即作废 */
    private static final int MAX_ERROR_COUNT = 5;

    /** 单用户 24 小时内最大发送量，防止通过本接口刷第三方邮箱 */
    private static final int MAX_DAILY_SEND = 10;

    /** 邮箱格式（长度上限由 sys_user.email varchar(50) 兜底） */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisCache redisCache;

    /** 发信地址，需与阿里云邮件推送控制台配置的自定义地址一致 */
    @Value("${spring.mail.username:}")
    private String from;

    /**
     * 发送邮箱验证码
     *
     * @param email 目标邮箱（新邮箱）
     * @param userId 当前登录用户
     */
    public void sendCode(String email, Long userId)
    {
        String target = StringUtils.trim(email);
        if (StringUtils.isEmpty(target))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(target).matches())
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "邮箱格式不正确");
        }
        if (StringUtils.isEmpty(from))
        {
            log.error("发送邮箱验证码失败：spring.mail.username 未配置");
            throw new ServiceException("邮件服务未配置，请联系管理员");
        }

        String limitKey = CacheConstants.EMAIL_CODE_LIMIT_KEY + userId;
        if (Boolean.TRUE.equals(redisCache.hasKey(limitKey)))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码发送过于频繁，请稍后再试");
        }

        String dailyKey = CacheConstants.EMAIL_CODE_DAILY_KEY + userId;
        String dailyValue = redisCache.getCacheObject(dailyKey);
        int dailyCount = StringUtils.isEmpty(dailyValue) ? 0 : Integer.parseInt(dailyValue);
        if (dailyCount >= MAX_DAILY_SEND)
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "今日验证码发送次数已达上限，请明日再试");
        }

        String code = randomCode();
        String cacheKey = codeKey(userId, target);
        redisCache.setCacheObject(cacheKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisCache.setCacheObject(dailyKey, String.valueOf(dailyCount + 1), 1, TimeUnit.DAYS);
        redisCache.deleteObject(errorKey(userId, target));
        try
        {
            send(target, code);
        }
        catch (Exception e)
        {
            // 发送失败立即作废验证码，避免「没收到邮件却能猜码」
            redisCache.deleteObject(cacheKey);
            log.error("邮箱验证码发送失败，email={}", target, e);
            throw new ServiceException("验证码发送失败，请稍后重试");
        }
        redisCache.setCacheObject(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 校验并消费验证码
     *
     * @param email 目标邮箱
     * @param userId 当前登录用户
     * @param code 用户提交的验证码
     */
    public void verify(String email, Long userId, String code)
    {
        if (StringUtils.isEmpty(code))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请输入邮箱验证码");
        }
        String target = StringUtils.trim(email);
        String cacheKey = codeKey(userId, target);
        String cached = redisCache.getCacheObject(cacheKey);
        if (StringUtils.isEmpty(cached))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码已失效，请重新获取");
        }
        if (!cached.equals(code.trim()))
        {
            recordError(userId, target, cacheKey);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码不正确");
        }
        redisCache.deleteObject(cacheKey);
        redisCache.deleteObject(errorKey(userId, target));
    }

    /**
     * 记录错误次数，达到上限直接作废验证码，防止暴力枚举
     */
    private void recordError(Long userId, String email, String codeKey)
    {
        String errKey = errorKey(userId, email);
        String cached = redisCache.getCacheObject(errKey);
        int count = StringUtils.isEmpty(cached) ? 0 : Integer.parseInt(cached);
        count++;
        if (count >= MAX_ERROR_COUNT)
        {
            redisCache.deleteObject(codeKey);
            redisCache.deleteObject(errKey);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码错误次数过多，请重新获取");
        }
        redisCache.setCacheObject(errKey, String.valueOf(count), CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    private void send(String email, String code) throws Exception
    {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject("Quico 邮箱验证码");
        helper.setText(buildContent(code), true);
        mailSender.send(message);
    }

    private String buildContent(String code)
    {
        return "<p>您正在修改 星铁助手 账号的绑定邮箱，本次验证码为：</p>"
                + "<p style=\"font-size:24px;font-weight:bold;letter-spacing:4px;\">" + code + "</p>"
                + "<p>验证码 " + CODE_EXPIRE_MINUTES + " 分钟内有效，请勿泄露给他人。</p>"
                + "<p>如非本人操作，请忽略本邮件。</p>";
    }

    private String randomCode()
    {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    private String codeKey(Long userId, String email)
    {
        return CacheConstants.EMAIL_CODE_KEY + userId + ":" + email;
    }

    private String errorKey(Long userId, String email)
    {
        return CacheConstants.EMAIL_CODE_ERROR_KEY + userId + ":" + email;
    }
}
