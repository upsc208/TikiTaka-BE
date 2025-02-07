package com.trillion.tikitaka.authentication.application.handler;

import com.trillion.tikitaka.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponder errorResponder;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("[인증] 인증되지 않은 사용자입니다.");
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        Object errorCodeAttr = request.getAttribute("JWT_ERROR_CODE");
        if (errorCodeAttr instanceof ErrorCode ec) {
            errorCode = ec;
        }

        errorResponder.sendErrorResponse(response, errorCode);
    }
}
