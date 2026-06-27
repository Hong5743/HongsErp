package com.hongs.hongs_erp.auth.adapter.out.redis;

import com.hongs.hongs_erp.auth.application.port.out.LoginFailPort;
import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisTokenAdapter implements TokenBlacklistPort, RefreshTokenPort, LoginFailPort {

    private static final String BLACKLIST_KEY = "auth:blacklist:";
    private static final String REFRESH_KEY = "auth:refresh:";
    private static final String LOGIN_FAIL_KEY = "auth:login-fail:";

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

    @Override
    public int incrementAndGet(Long userId) {
        Long count = redisTemplate.opsForValue().increment(LOGIN_FAIL_KEY + userId);
        return count == null ? 1 : count.intValue();
    }

    @Override
    public void reset(Long userId) {
        redisTemplate.delete(LOGIN_FAIL_KEY + userId);
    }
}
