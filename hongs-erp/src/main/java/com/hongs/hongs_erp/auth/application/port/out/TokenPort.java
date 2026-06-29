package com.hongs.hongs_erp.auth.application.port.out;

import com.hongs.hongs_erp.employee.domain.User;

import java.util.Optional;

public interface TokenPort {
    String createAccessToken(User user);
    String createRefreshToken(Long userId);
    ParsedToken parseToken(String token);
    Optional<String> parseSubjectIgnoreExpiry(String token);
    long getRefreshTokenExpirySeconds();
}
