package com.hongs.hongs_erp.auth.adapter.in.web;

import com.hongs.hongs_erp.auth.application.dto.request.LoginRequest;
import com.hongs.hongs_erp.auth.application.dto.response.TokenPair;
import com.hongs.hongs_erp.auth.application.port.in.LoginUseCase;
import com.hongs.hongs_erp.auth.application.port.in.LogoutUseCase;
import com.hongs.hongs_erp.auth.application.port.in.RefreshTokenUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final int accessTokenMaxAge;
    private final int refreshTokenMaxAge;

    public AuthController(
            LoginUseCase loginUseCase,
            LogoutUseCase logoutUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            @Value("${jwt.access-token-expiry-seconds}") int accessTokenMaxAge,
            @Value("${jwt.refresh-token-expiry-seconds}") int refreshTokenMaxAge) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.accessTokenMaxAge = accessTokenMaxAge;
        this.refreshTokenMaxAge = refreshTokenMaxAge;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        TokenPair tokens = loginUseCase.login(request);
        setAccessTokenCookie(response, tokens.accessToken());
        setRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(Map.of("message", "로그인 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        extractCookie(request, "access_token").ifPresent(logoutUseCase::logout);
        clearCookie(response, "access_token", "/");
        clearCookie(response, "refresh_token", "/api/auth/refresh");
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token")
                .orElseThrow(() -> new com.hongs.hongs_erp.global.exception.AuthException("Refresh Token이 없습니다", 401));

        TokenPair tokens = refreshTokenUseCase.refresh(refreshToken);
        setAccessTokenCookie(response, tokens.accessToken());
        setRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(Map.of("message", "토큰 갱신 성공"));
    }

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Strict");
        cookie.setMaxAge(accessTokenMaxAge);
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setAttribute("SameSite", "Strict");
        cookie.setMaxAge(refreshTokenMaxAge);
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<String> extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
