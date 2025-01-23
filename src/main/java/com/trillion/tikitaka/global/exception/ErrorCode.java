package com.trillion.tikitaka.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C0001", "서버에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, "C0002", "잘못된 요청값입니다."),

    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A0001", "인증되지 않은 사용자입니다."),
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.BAD_REQUEST, "A0002", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "A0003", "계정이 잠겨있습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
