package com.hongs.hongs_erp.auth.application.port.in;

import com.hongs.hongs_erp.auth.application.dto.request.LoginRequest;
import com.hongs.hongs_erp.auth.application.dto.response.TokenPair;

public interface LoginUseCase {
    TokenPair login(LoginRequest request);
}
