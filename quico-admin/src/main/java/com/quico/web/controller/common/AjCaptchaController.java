package com.quico.web.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.utils.StringUtils;
import com.quico.framework.web.service.VerificationCodeSwitch;

/**
 * 滑块验证码（aj-captcha）
 *
 * @author quico
 */
@RestController
@RequestMapping("/captcha")
public class AjCaptchaController
{
    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private VerificationCodeSwitch verificationCodeSwitch;

    /**
     * 查询验证码开关
     */
    @GetMapping("/enabled")
    public AjaxResult enabled()
    {
        // captchaEnabled：图形验证码开关（sys.account.captchaEnabled）
        // ajCaptchaCheck：滑动验证码开关（sys.account.ajCaptchaCheck）
        return AjaxResult.success()
                .put("captchaEnabled", verificationCodeSwitch.captchaEnabled())
                .put("ajCaptchaCheck", verificationCodeSwitch.ajCaptchaCheckEnabled());
    }

    /**
     * 获取验证码
     */
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request)
    {
        if (!verificationCodeSwitch.ajCaptchaCheckEnabled())
        {
            return ResponseModel.successMsg("滑动验证码已关闭");
        }
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.get(data);
    }

    /**
     * 校验验证码
     */
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request)
    {
        if (!verificationCodeSwitch.ajCaptchaCheckEnabled())
        {
            return ResponseModel.successMsg("滑动验证码已关闭");
        }
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.check(data);
    }

    public static String getRemoteId(HttpServletRequest request)
    {
        String ip = getRemoteIpFromXfwd(request.getHeader("X-Forwarded-For"));
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotEmpty(ip))
        {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromXfwd(String xfwd)
    {
        if (StringUtils.isNotEmpty(xfwd))
        {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[ipList.length - 1]);
        }
        return null;
    }
}
