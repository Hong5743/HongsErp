package com.hongs.hongs_erp.auth.adapter.out.redis;

import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisBlacklistAdapter implements TokenBlacklistPort {

    private static final String BLACKLIST_KEY = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisBlacklistAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklist(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(BLACKLIST_KEY + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY + jti));
    }
}
