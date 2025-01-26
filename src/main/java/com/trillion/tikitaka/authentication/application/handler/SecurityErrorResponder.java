package com.trillion.tikitaka.authentication.application.handler;

import com.trillion.tikitaka.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface SecurityErrorResponder {
    void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException;
}
