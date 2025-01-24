package com.trillion.tikitaka.authentication.application;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.dto.response.TokenResponse;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final JwtTokenRepository jwtTokenRepository;

    public TokenResponse reissueTokens(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return null;
        }

        validateRefreshToken(refreshToken);

        Boolean isRefreshTokenExist = jwtTokenRepository.existsByRefreshToken(refreshToken);
        if (!isRefreshTokenExist) {
            return null;
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, username, role, ACCESS_TOKEN_EXPIRATION);
        String newRefreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, username, role, REFRESH_TOKEN_EXPIRATION);

        jwtTokenRepository.deleteByRefreshToken(refreshToken);
        jwtUtil.addRefreshToken(username, newRefreshToken, REFRESH_TOKEN_EXPIRATION);

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
            throw new RuntimeException();
        }

        String type = jwtUtil.getType(refreshToken);
        if (!type.equals(TOKEN_TYPE_REFRESH)) {
            throw new RuntimeException();
        }
    }
}
