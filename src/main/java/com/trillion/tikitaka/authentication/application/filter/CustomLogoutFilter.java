package com.trillion.tikitaka.authentication.application.filter;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.DEFAULT_FILTER_HTTP_METHOD;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_REFRESH;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final JwtTokenRepository jwtTokenRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        if (!requestURI.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals(DEFAULT_FILTER_HTTP_METHOD)) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN_TYPE_REFRESH)) {
                refreshToken = cookie.getValue();
            }
        }

        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String type = jwtUtil.getType(refreshToken);
        if (!type.equals(TOKEN_TYPE_REFRESH)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Refresh Token 존재 여부 확인
        Boolean isRefreshTokenExist = jwtTokenRepository.existsByRefreshToken(refreshToken);
        if (!isRefreshTokenExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        jwtTokenRepository.deleteByRefreshToken(refreshToken);

        Cookie cookie = new Cookie(TOKEN_TYPE_REFRESH, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
