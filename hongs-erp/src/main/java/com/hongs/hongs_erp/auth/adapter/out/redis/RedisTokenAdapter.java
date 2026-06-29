package com.hongs.hongs_erp.auth.adapter.out.redis;

import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisTokenAdapter implements TokenBlacklistPort, RefreshTokenPort {

    private static final String BLACKLIST_KEY = "auth:blacklist:";
    private static final String REFRESH_KEY = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenAdapter(StringRedisTemplate redisTemplate) {
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

    @Override
    public void store(Long userId, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(REFRESH_KEY + userId, refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_KEY + userId));
    }

    @Override
    public void delete(Long userId) {
        redisTemplate.delete(REFRESH_KEY + userId);
    }
}
