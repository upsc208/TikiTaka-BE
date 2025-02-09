package com.trillion.tikitaka.authentication.application;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.dto.response.TokenResponse;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final JwtTokenRepository jwtTokenRepository;

    @Transactional
    public TokenResponse reissueTokens(HttpServletRequest request) {
        log.info("[토큰 재발급 요청]");
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            log.info("[토큰 재발급 요청] 리프레시 토큰이 존재하지 않습니다.");
            return null;
        }

        validateRefreshToken(refreshToken);

        Boolean isRefreshTokenExist = jwtTokenRepository.existsByRefreshToken(refreshToken);
        if (!isRefreshTokenExist) {
            log.info("[토큰 재발급 요청] 리프레시 토큰이 존재하지 않습니다.");
            return null;
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, userId, username, role, ACCESS_TOKEN_EXPIRATION);
        String newRefreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, userId, username, role, REFRESH_TOKEN_EXPIRATION);

        jwtTokenRepository.deleteByRefreshToken(refreshToken);
        jwtUtil.addRefreshToken(username, newRefreshToken, REFRESH_TOKEN_EXPIRATION);

        log.info("[토큰 재발급 요청] 토큰 재발급 완료");
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN_TYPE_REFRESH)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void validateRefreshToken(String refreshToken) {
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            log.error("[토큰 재발급 요청] 리프레시 토큰이 만료되었습니다.");
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        String type = jwtUtil.getType(refreshToken);
        if (!type.equals(TOKEN_TYPE_REFRESH)) {
            log.error("[토큰 재발급 요청] 잘못된 리프레시 토큰압니다.");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
