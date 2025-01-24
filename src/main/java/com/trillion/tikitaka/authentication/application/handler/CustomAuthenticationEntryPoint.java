package com.trillion.tikitaka.authentication.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.CONTENT_TYPE;
import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.ENCODING;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.UNAUTHORIZED.getHttpStatus(),
                ErrorCode.UNAUTHORIZED.getErrorCode(),
                ErrorCode.UNAUTHORIZED.getMessage()
        );

        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(ENCODING);
        response.getWriter().write(responseBody);
    }
}
