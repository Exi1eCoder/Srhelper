package com.quico.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.quico.common.utils.StringUtils;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;

/**
 * 微信小程序服务配置
 *
 * @author quico
 */
@Configuration
public class WxMaConfiguration
{
    /** 小程序AppId */
    @Value("${wechat.miniapp.appid:}")
    private String appid;

    /** 小程序AppSecret */
    @Value("${wechat.miniapp.secret:}")
    private String secret;

    @Bean
    public WxMaService wxMaService()
    {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(StringUtils.trim(appid));
        config.setSecret(StringUtils.trim(secret));
        // 登录链路只做一次会话换取，重试次数保持最小，避免微信接口异常时长时间阻塞
        config.setMaxRetryTimes(1);
        config.setRetrySleepMillis(1000);

        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(config);
        return wxMaService;
    }
}
