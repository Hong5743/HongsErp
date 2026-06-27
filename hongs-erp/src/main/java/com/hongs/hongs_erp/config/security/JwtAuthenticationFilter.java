package com.hongs.hongs_erp.config.security;

import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistPort tokenBlacklistPort;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistPort tokenBlacklistPort) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistPort = tokenBlacklistPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Optional<String> tokenOpt = extractTokenFromCookie(request);
        if (tokenOpt.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String token = tokenOpt.get();
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            String jti = claims.getId();

            boolean blacklisted = tokenBlacklistPort.isBlacklisted(jti);
            if (blacklisted) {
                sendUnauthorized(response);
                return;
            }

            String role = claims.get("role", String.class);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (DataAccessException e) {
            sendServiceUnavailable(response);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            sendUnauthorized(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"errors\":[{\"field\":\"\",\"message\":\"인증이 필요합니다\"}]}");
    }

    private void sendServiceUnavailable(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"errors\":[{\"field\":\"\",\"message\":\"서비스를 일시적으로 사용할 수 없습니다\"}]}");
    }
}
