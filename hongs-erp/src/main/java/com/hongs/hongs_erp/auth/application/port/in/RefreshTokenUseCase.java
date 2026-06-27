package com.hongs.hongs_erp.auth.application.port.in;

import com.hongs.hongs_erp.auth.application.dto.response.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refresh(String refreshToken);
}
