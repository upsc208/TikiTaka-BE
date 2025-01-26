package com.trillion.tikitaka.authentication.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.CONTENT_TYPE;
import static com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter.ENCODING;

@Component
public class SecurityErrorResponderImpl implements SecurityErrorResponder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getHttpStatus(),
                errorCode.getErrorCode(),
                errorCode.getMessage()
        );
        response.setContentType(CONTENT_TYPE);
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding(ENCODING);

        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(responseBody);
    }
}
