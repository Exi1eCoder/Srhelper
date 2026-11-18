package com.quico.framework.web.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.quico.common.constant.CacheConstants;
import com.quico.common.constant.HttpStatus;
import com.quico.common.core.redis.RedisCache;
import com.quico.common.exception.BusinessException;
import com.quico.common.exception.ServiceException;
import com.quico.common.utils.StringUtils;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 手机验证码服务
 *
 * 用于个人中心修改绑定手机号前的身份校验：验证码发送到「新手机号」，
 * 校验通过即证明当前用户可控制该号码。短信通道使用腾讯云 SMS。
 *
 * @author quico
 */
@Slf4j
@Service
public class SmsVerifyCodeService
{
    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /** 同一用户两次发送的最小间隔（秒） */
    private static final int SEND_INTERVAL_SECONDS = 60;

    /** 单个验证码允许的最大错误次数，超过即作废 */
    private static final int MAX_ERROR_COUNT = 5;

    /** 单用户 24 小时内最大发送量，防止通过本接口刷短信 */
    private static final int MAX_DAILY_SEND = 10;

    /** 中国大陆手机号（长度上限由 sys_user.phonenumber varchar(11) 兜底） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 腾讯云短信接口域名 */
    private static final String SMS_ENDPOINT = "sms.tencentcloudapi.com";

    /** 腾讯云短信发送成功状态码 */
    private static final String SMS_SUCCESS_CODE = "Ok";

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 短信客户端惰性初始化，避免配置缺失时应用启动失败 */
    private volatile SmsClient smsClient;

    @Autowired
    private RedisCache redisCache;

    /** 腾讯云 API 密钥 SecretId，通过环境变量注入，禁止写入仓库 */
    @Value("${sms.tencent.secret-id:}")
    private String secretId;

    /** 腾讯云 API 密钥 SecretKey，通过环境变量注入，禁止写入仓库 */
    @Value("${sms.tencent.secret-key:}")
    private String secretKey;

    /** 短信应用 SdkAppId */
    @Value("${sms.tencent.sdk-app-id:}")
    private String sdkAppId;

    /** 短信签名内容 */
    @Value("${sms.tencent.sign-name:}")
    private String signName;

    /** 短信模板 ID（模板变量两个：验证码、有效期分钟数） */
    @Value("${sms.tencent.template-id:}")
    private String templateId;

    /** 短信接口地域 */
    @Value("${sms.tencent.region:ap-guangzhou}")
    private String region;

    /**
     * 发送手机验证码
     *
     * @param phonenumber 目标手机号（新手机号）
     * @param userId 当前登录用户
     */
    public void sendCode(String phonenumber, Long userId)
    {
        String target = StringUtils.trim(phonenumber);
        if (StringUtils.isEmpty(target))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(target).matches())
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "手机号格式不正确");
        }
        ensureConfigured();

        String limitKey = CacheConstants.PHONE_CODE_LIMIT_KEY + userId;
        if (Boolean.TRUE.equals(redisCache.hasKey(limitKey)))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码发送过于频繁，请稍后再试");
        }

        String dailyKey = CacheConstants.PHONE_CODE_DAILY_KEY + userId;
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
            // 发送失败立即作废验证码，避免「没收到短信却能猜码」
            redisCache.deleteObject(cacheKey);
            log.error("手机验证码发送失败，phone={}", mask(target), e);
            throw new ServiceException("验证码发送失败，请稍后重试");
        }
        redisCache.setCacheObject(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 校验并消费验证码
     *
     * @param phonenumber 目标手机号
     * @param userId 当前登录用户
     * @param code 用户提交的验证码
     */
    public void verify(String phonenumber, Long userId, String code)
    {
        if (StringUtils.isEmpty(code))
        {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请输入手机验证码");
        }
        String target = StringUtils.trim(phonenumber);
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
    private void recordError(Long userId, String phonenumber, String codeKey)
    {
        String errKey = errorKey(userId, phonenumber);
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

    /**
     * 校验短信配置是否完整，避免带着空配置调用第三方接口
     */
    private void ensureConfigured()
    {
        if (StringUtils.isEmpty(secretId) || StringUtils.isEmpty(secretKey)
                || StringUtils.isEmpty(sdkAppId) || StringUtils.isEmpty(signName)
                || StringUtils.isEmpty(templateId))
        {
            log.error("发送手机验证码失败：腾讯云短信配置不完整");
            throw new ServiceException("短信服务未配置，请联系管理员");
        }
    }

    private void send(String phonenumber, String code) throws Exception
    {
        SendSmsRequest request = new SendSmsRequest();
        request.setSmsSdkAppId(sdkAppId);
        request.setSignName(signName);
        request.setTemplateId(templateId);
        request.setPhoneNumberSet(new String[] { "+86" + phonenumber });
        request.setTemplateParamSet(new String[] { code, String.valueOf(CODE_EXPIRE_MINUTES) });

        SendSmsResponse response = getClient().SendSms(request);
        SendStatus[] statuses = response.getSendStatusSet();
        if (statuses == null || statuses.length == 0)
        {
            throw new ServiceException("短信发送失败：腾讯云未返回发送状态");
        }
        SendStatus status = statuses[0];
        if (!SMS_SUCCESS_CODE.equalsIgnoreCase(status.getCode()))
        {
            throw new ServiceException("短信发送失败：" + status.getMessage());
        }
    }

    private SmsClient getClient()
    {
        SmsClient client = smsClient;
        if (client == null)
        {
            synchronized (this)
            {
                client = smsClient;
                if (client == null)
                {
                    Credential credential = new Credential(secretId, secretKey);
                    HttpProfile httpProfile = new HttpProfile();
                    httpProfile.setEndpoint(SMS_ENDPOINT);
                    ClientProfile clientProfile = new ClientProfile();
                    clientProfile.setHttpProfile(httpProfile);
                    client = new SmsClient(credential, region, clientProfile);
                    smsClient = client;
                }
            }
        }
        return client;
    }

    private String randomCode()
    {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    private String codeKey(Long userId, String phonenumber)
    {
        return CacheConstants.PHONE_CODE_KEY + userId + ":" + phonenumber;
    }

    private String errorKey(Long userId, String phonenumber)
    {
        return CacheConstants.PHONE_CODE_ERROR_KEY + userId + ":" + phonenumber;
    }

    /**
     * 日志脱敏：保留前 3 位与后 4 位
     */
    private String mask(String phonenumber)
    {
        if (phonenumber.length() < 7)
        {
            return "***";
        }
        return phonenumber.substring(0, 3) + "****" + phonenumber.substring(phonenumber.length() - 4);
    }
}
