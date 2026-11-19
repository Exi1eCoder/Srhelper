package com.quico.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.quico.common.core.text.Convert;
import com.quico.common.utils.StringUtils;
import com.quico.system.service.ISysConfigService;

/**
 * 验证码开关统一出口
 *
 * 设计约定：影响业务分支的配置开关统一收口到此类组件，
 * 避免 Controller/Service 直接调 configService.selectConfigByKey() 导致
 * 默认值逻辑散落、多处复制。新增开关时在此类加方法，不要在调用方直接读配置。
 * 
 * @author quico
 */
@Component
public class VerificationCodeSwitch
{
    /** 邮箱验证码开关参数 */
    private static final String MAIL_VERIFICATION_CODE_KEY = "sys.account.mailVerificationCode";

    /** 手机验证码开关参数 */
    private static final String PHONE_VERIFICATION_CODE_KEY = "sys.account.phoneVerificationCode";

    /** 滑动验证码开关参数 */
    private static final String AJ_CAPTCHA_CHECK_KEY = "sys.account.ajCaptchaCheck";

    @Autowired
    private ISysConfigService configService;

    /**
     * 图形验证码开关
     */
    public boolean captchaEnabled()
    {
        return configService.selectCaptchaEnabled();
    }

    /**
     * 邮箱验证码开关
     */
    public boolean mailVerificationCodeEnabled()
    {
        return isEnabled(MAIL_VERIFICATION_CODE_KEY);
    }

    /**
     * 手机验证码开关
     */
    public boolean phoneVerificationCodeEnabled()
    {
        return isEnabled(PHONE_VERIFICATION_CODE_KEY);
    }

    /**
     * 滑动验证码开关
     */
    public boolean ajCaptchaCheckEnabled()
    {
        return isEnabled(AJ_CAPTCHA_CHECK_KEY);
    }

    private boolean isEnabled(String configKey)
    {
        String enabled = configService.selectConfigByKey(configKey);
        if (StringUtils.isEmpty(enabled))
        {
            return true;
        }
        return Convert.toBool(enabled, Boolean.TRUE);
    }
}
