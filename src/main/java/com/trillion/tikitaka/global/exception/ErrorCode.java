package com.trillion.tikitaka.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청값입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "요청값을 입력해주세요."),
    INVALID_REQUEST_VALUE(HttpStatus.BAD_REQUEST, "C004", "요청값이 잘못되었습니다."),

    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 사용자입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.BAD_REQUEST, "A003", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "A004", "계정이 잠겨있습니다. 잠시 후 다시 시도해주세요."),

    // Token
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "토큰이 만료되었습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "T003", "잘못된 토큰 서명입니다."),

    // Registration
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "R001", "이미 사용중인 아이디입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "R002", "이미 사용중인 이메일입니다."),
    REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R003", "계정 등록 요청을 찾을 수 없습니다."),
    REGISTRATION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "R004", "이미 처리된 계정 등록 요청입니다."),

    // Ticket Type
    TICKET_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "TT001", "티켓 유형을 찾을 수 없습니다."),
    DUPLICATED_TICKET_TYPE(HttpStatus.CONFLICT, "TT002", "이미 존재하는 티켓 유형입니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
