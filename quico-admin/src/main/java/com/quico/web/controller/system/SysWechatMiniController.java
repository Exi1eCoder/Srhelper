package com.quico.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quico.common.annotation.Anonymous;
import com.quico.common.annotation.RateLimiter;
import com.quico.common.constant.CacheConstants;
import com.quico.common.constant.Constants;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.core.domain.model.WechatMiniLoginBody;
import com.quico.common.enums.LimitType;
import com.quico.framework.web.service.WechatMiniLoginService;

/**
 * 微信小程序登录
 *
 * @author quico
 */
@RestController
@RequestMapping("/system/auth/wechat-mini")
public class SysWechatMiniController
{
    @Autowired
    private WechatMiniLoginService wechatMiniLoginService;

    /**
     * 微信小程序一键登录
     *
     * @param body 登录信息，仅需 code
     * @return 结果
     */
    @Anonymous
    @RateLimiter(key = CacheConstants.RATE_LIMIT_KEY + "wechat_mini_ip:", time = 60, count = 10, limitType = LimitType.IP)
    @PostMapping("/login")
    public AjaxResult login(@RequestBody WechatMiniLoginBody body)
    {
        AjaxResult ajax = AjaxResult.success();
        String token = wechatMiniLoginService.login(body.getCode());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }
}
