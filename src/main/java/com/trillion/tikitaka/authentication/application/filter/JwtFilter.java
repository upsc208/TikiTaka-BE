package com.trillion.tikitaka.authentication.application.filter;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.user.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.*;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = request.getHeader(TOKEN_HEADER);
            if (accessToken == null || !accessToken.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            accessToken = accessToken.substring(7).trim();

            validateToken(accessToken);

            setAuthentication(accessToken);

            filterChain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            request.setAttribute("JWT_ERROR_CODE", ErrorCode.INVALID_TOKEN);
            throw new MalformedJwtException("잘못된 토큰 형식");
        } catch (ExpiredJwtException e) {
            request.setAttribute("JWT_ERROR_CODE", ErrorCode.EXPIRED_TOKEN);
            throw new ExpiredJwtException(null, null, "만료된 토큰");
        } catch (SignatureException e) {
            request.setAttribute("JWT_ERROR_CODE", ErrorCode.INVALID_SIGNATURE);
            throw new SignatureException("잘못된 서명");
        } catch (Exception e) {
            request.setAttribute("JWT_ERROR_CODE", ErrorCode.INTERNAL_SERVER_ERROR);
            throw new InsufficientAuthenticationException("인증 실패");
        }
    }

    private void validateToken(String token) {
        if (jwtUtil.isExpired(token)) {
            throw new ExpiredJwtException(null, null, "만료된 토큰");
        }
        String type = jwtUtil.getType(token);
        if (!TOKEN_TYPE_ACCESS.equals(type)) {
            throw new MalformedJwtException("잘못된 토큰 타입");
        }
    }

    private void setAuthentication(String token) {
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        CustomUserDetails userDetails = new CustomUserDetails(new User(userId, username, role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }
}
