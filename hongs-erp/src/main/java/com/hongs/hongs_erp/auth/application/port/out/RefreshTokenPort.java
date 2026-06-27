package com.hongs.hongs_erp.auth.application.port.out;

import java.util.Optional;

public interface RefreshTokenPort {
    void store(Long userId, String refreshToken, long ttlSeconds);
    Optional<String> findByUserId(Long userId);
    void delete(Long userId);
}
