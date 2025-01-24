package com.trillion.tikitaka.authentication.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.CONTENT_TYPE;
import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.ENCODING;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.ACCESS_DENIED.getHttpStatus(),
                ErrorCode.ACCESS_DENIED.getErrorCode(),
                ErrorCode.ACCESS_DENIED.getMessage()
        );

        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(ENCODING);
        response.getWriter().write(responseBody);
    }
}
