package com.hongs.hongs_erp.auth.application.port.out;

import com.hongs.hongs_erp.employee.domain.User;

public interface TokenPort {
    String createAccessToken(User user);
    String createRefreshToken(Long userId);
    ParsedToken parseToken(String token);
    long getRefreshTokenExpirySeconds();
}
