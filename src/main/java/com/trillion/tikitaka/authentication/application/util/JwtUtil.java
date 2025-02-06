package com.trillion.tikitaka.authentication.application.util;

import com.trillion.tikitaka.authentication.domain.JwtToken;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final Long ACCESS_TOKEN_EXPIRATION = 300000L;
    public static final Long REFRESH_TOKEN_EXPIRATION = 86400000L;

    private SecretKey secretKey;
    private final JwtTokenRepository jwtTokenRepository;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret, JwtTokenRepository jwtTokenRepository) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS512.key().build().getAlgorithm()
        );
        this.jwtTokenRepository = jwtTokenRepository;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("id", Long.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    public String createJwtToken(String type, Long userId, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("type", type)
                .claim("id", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public ResponseCookie createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .maxAge(60 * 60 * 10)
                .path("/")
                .build();
    }

    public void addRefreshToken(String username, String refreshToken, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        JwtToken jwtToken = JwtToken.builder()
                .username(username)
                .refreshToken(refreshToken)
                .expiration(date)
                .build();

        jwtTokenRepository.save(jwtToken);
    }
}
