package com.quico.framework.web.service;

import com.anji.captcha.service.CaptchaCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CaptchaRedisService implements CaptchaCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        redisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 核心修正点：接口要求的方法名是 type()，返回缓存类型标识
     */
    @Override
    public String type() {
        return "redis";
    }
}