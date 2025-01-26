package com.trillion.tikitaka.authentication.application.handler;

import com.trillion.tikitaka.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final SecurityErrorResponder errorResponder;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException failed) throws IOException {
        ErrorCode errorCode;
        if (failed instanceof BadCredentialsException){
            errorCode = ErrorCode.INVALID_USERNAME_OR_PASSWORD;
        } else if(failed instanceof LockedException){
            errorCode = ErrorCode.ACCOUNT_LOCKED;
        } else {
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        }
        errorResponder.sendErrorResponse(response, errorCode);
    }
}
