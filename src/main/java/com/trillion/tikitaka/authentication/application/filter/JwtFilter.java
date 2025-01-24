package com.trillion.tikitaka.authentication.application.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.user.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.CONTENT_TYPE;
import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.ENCODING;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.*;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 헤더에서 토큰 조회
            String accessToken = request.getHeader(TOKEN_HEADER);
            if (accessToken == null || !accessToken.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            accessToken = accessToken.substring(7).trim();

            // 토큰 검증
            validateToken(accessToken);

            // 사용자 인증 설정
            setAuthentication(accessToken);

            filterChain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN);
        } catch (SignatureException e) {
            sendErrorResponse(response, ErrorCode.INVALID_SIGNATURE);
        } catch (Exception e) {
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateToken(String token) {
        if (jwtUtil.isExpired(token)) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
        String type = jwtUtil.getType(token);
        if (!TOKEN_TYPE_ACCESS.equals(type)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void setAuthentication(String token) {
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        CustomUserDetails userDetails = new CustomUserDetails(new User(username, role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getHttpStatus(),
                errorCode.getErrorCode(),
                errorCode.getMessage()
        );

        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        response.getWriter().write(responseBody);
    }
}
