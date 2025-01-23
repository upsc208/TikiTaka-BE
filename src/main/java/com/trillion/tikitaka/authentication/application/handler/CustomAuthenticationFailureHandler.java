package com.trillion.tikitaka.authentication.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.CONTENT_TYPE;
import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.ENCODING;

@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException failed) throws IOException {
        if (failed instanceof BadCredentialsException){
            sendErrorResponse(response, new CustomException(ErrorCode.INVALID_USERNAME_OR_PASSWORD));
        } else if(failed instanceof LockedException){
            sendErrorResponse(response, new CustomException(ErrorCode.ACCOUNT_LOCKED));
        } else{
            sendErrorResponse(response, new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    private void sendErrorResponse(HttpServletResponse response, CustomException e) throws IOException {
        ErrorCode errorCode = e.getErrorCode();
        response.setStatus(errorCode.getHttpStatus().value());
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getHttpStatus(),
                errorCode.getErrorCode(),
                errorCode.getMessage()
        );

        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(ENCODING);
        response.getWriter().write(responseBody);
    }
}
