package com.trillion.tikitaka.authentication.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.domain.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.CONTENT_TYPE;
import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.ENCODING;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.*;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = iterator.next().getAuthority();

        // 토큰 생성
        String accessToken = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, username, role, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, username, role, REFRESH_TOKEN_EXPIRATION);

        // Refresh Token 저장
        jwtUtil.addRefreshToken(username, refreshToken, REFRESH_TOKEN_EXPIRATION);

        response.addHeader(TOKEN_HEADER, TOKEN_PREFIX + accessToken);
        response.addCookie(jwtUtil.createCookie(TOKEN_TYPE_REFRESH, refreshToken));

        // 비밀번호 변경 필요 여부 확인
        boolean passwordChangeNeeded = false;
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
            if (user.getLastPasswordChangedAt() == null
                    || user.getLastPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(90))) {
                passwordChangeNeeded = true;
            }
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("passwordChangeNeeded", passwordChangeNeeded);

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>("로그인에 성공했습니다.", responseData);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        String responseJson = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(responseJson);
    }
}
