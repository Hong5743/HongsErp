package com.hongs.hongs_erp.auth.application.service;

import com.hongs.hongs_erp.auth.application.dto.request.LoginRequest;
import com.hongs.hongs_erp.auth.application.dto.response.TokenPair;
import com.hongs.hongs_erp.auth.application.port.in.LoginUseCase;
import com.hongs.hongs_erp.auth.application.port.in.LogoutUseCase;
import com.hongs.hongs_erp.auth.application.port.in.RefreshTokenUseCase;
import com.hongs.hongs_erp.auth.application.port.out.ParsedToken;
import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.global.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService implements LoginUseCase, LogoutUseCase, RefreshTokenUseCase {

    private static final String INVALID_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다";

    private final UserRepository userRepository;
    private final TokenPort tokenPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenPort refreshTokenPort;
    private final int maxFailCount;
    private final String allowedDomain;

    public AuthService(
            UserRepository userRepository,
            TokenPort tokenPort,
            PasswordEncoder passwordEncoder,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenPort refreshTokenPort,
            @Value("${auth.max-fail-count}") int maxFailCount,
            @Value("${auth.allowed-domains}") String allowedDomain) {
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistPort = tokenBlacklistPort;
        this.refreshTokenPort = refreshTokenPort;
        this.maxFailCount = maxFailCount;
        this.allowedDomain = allowedDomain;
    }

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public TokenPair login(LoginRequest request) {
        validateEmailDomain(request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(INVALID_CREDENTIALS, 401));

        if (user.isLocked()) {
            throw new AuthException(INVALID_CREDENTIALS, 401);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            int newFailCount = userRepository.incrementFailCountAndGet(user.getId());
            if (newFailCount >= maxFailCount) {
                User freshUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new AuthException(INVALID_CREDENTIALS, 401));
                userRepository.update(freshUser.lock());
            }
            throw new AuthException(INVALID_CREDENTIALS, 401);
        }

        userRepository.update(user.resetFailCount());

        String accessToken = tokenPort.createAccessToken(user);
        String refreshToken = tokenPort.createRefreshToken(user.getId());
        refreshTokenPort.store(user.getId(), refreshToken, tokenPort.getRefreshTokenExpirySeconds());

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public void logout(String accessToken) {
        Long userId = null;
        try {
            ParsedToken parsed = tokenPort.parseToken(accessToken);
            if (parsed.remainingSeconds() > 0) {
                tokenBlacklistPort.blacklist(parsed.tokenId(), parsed.remainingSeconds());
            }
            userId = Long.valueOf(parsed.subject());
        } catch (AuthException ignored) {
            // 만료된 토큰은 블랙리스트 불필요, subject만 추출
            userId = tokenPort.parseSubjectIgnoreExpiry(accessToken)
                    .flatMap(s -> {
                        try {
                            return Optional.of(Long.valueOf(s));
                        } catch (NumberFormatException e) {
                            return Optional.empty();
                        }
                    })
                    .orElse(null);
        } catch (NumberFormatException ignored) {
            // 변조된 토큰의 subject가 숫자가 아님
        }
        if (userId != null) {
            refreshTokenPort.delete(userId);
        }
    }

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public TokenPair refresh(String oldRefreshToken) {
        ParsedToken parsed;
        try {
            parsed = tokenPort.parseToken(oldRefreshToken);
        } catch (AuthException e) {
            throw new AuthException("유효하지 않은 Refresh Token입니다", 401);
        }

        Long userId = Long.valueOf(parsed.subject());
        String stored = refreshTokenPort.findByUserId(userId)
                .orElseThrow(() -> new AuthException("유효하지 않은 Refresh Token입니다", 401));

        if (!stored.equals(oldRefreshToken)) {
            // 이미 사용된 구 토큰 → 탈취 의심 → 강제 로그아웃
            refreshTokenPort.delete(userId);
            throw new AuthException("보안 위반이 감지되었습니다. 다시 로그인하세요", 401);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다", 401));

        if (user.isLocked()) {
            refreshTokenPort.delete(userId);
            throw new AuthException(INVALID_CREDENTIALS, 401);
        }

        refreshTokenPort.delete(userId);

        String newAccessToken = tokenPort.createAccessToken(user);
        String newRefreshToken = tokenPort.createRefreshToken(userId);
        refreshTokenPort.store(userId, newRefreshToken, tokenPort.getRefreshTokenExpirySeconds());

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    private void validateEmailDomain(String email) {
        int atIndex = email == null ? -1 : email.indexOf('@');
        if (atIndex < 1) {
            throw new AuthException("사내 이메일 주소(@" + allowedDomain + ")만 허용됩니다", 400);
        }
        String domain = email.substring(atIndex + 1);
        if (!allowedDomain.equals(domain)) {
            throw new AuthException("사내 이메일 주소(@" + allowedDomain + ")만 허용됩니다", 400);
        }
    }
}
