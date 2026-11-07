package com.quico.srhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.quico.QuicoApplication;

/**
 * 集成测试：验证 Spring Boot 环境下 @Value 能否从 test profile 正常读取配置
 */
@SpringBootTest(classes = QuicoApplication.class)
@ActiveProfiles("test")
class WechatConfigInjectionIntegrationTest {

    @Value("${wechat.appid}")
    private String appid;

    /**
     * test profile 中存在 wechat.appid，@Value 注入结果不应为空
     */
    @Test
    void shouldInjectWechatAppid() {
        assertThat(appid)
                .as("wechat.appid 应通过 @Value 成功注入")
                .isNotBlank();
    }
}
