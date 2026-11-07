package com.quico.srhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.quico.QuicoApplication;

/**
 * 集成测试：验证 test 环境的 Redis 连接与 StringRedisTemplate 装配是否正常
 */
@SpringBootTest(classes = QuicoApplication.class)
@ActiveProfiles("test")
class RedisTemplateIntegrationTest {

    private static final String TEST_KEY = "quico:test:redis-template";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 写入 → 读回 → 清理，验证 StringRedisTemplate 可正常读写 test 环境 Redis
     */
    @Test
    void shouldReadBackWrittenValue() {
        redisTemplate.opsForValue().set(TEST_KEY, "ok", Duration.ofMinutes(1));
        try {
            assertThat(redisTemplate.opsForValue().get(TEST_KEY))
                    .as("写入 Redis 的值应能被原样读回")
                    .isEqualTo("ok");
        } finally {
            redisTemplate.delete(TEST_KEY);
        }
    }
}
