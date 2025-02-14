package com.trillion.tikitaka.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, "C002", "요청 값 타입이 잘못되었습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "요청 값이 누락되었습니다."),
    INVALID_REQUEST_VALUE(HttpStatus.BAD_REQUEST, "C004", "요청 값이 잘못되었습니다."),

    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 사용자입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.BAD_REQUEST, "A003", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "A004", "계정이 잠겨있습니다. 잠시 후 다시 시도해주세요."),

    // Inquiry
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "문의사항을 찾을 수 없습니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "I002", "이미 답변이 작성된 문의사항입니다."),

    // Token
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "토큰이 만료되었습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "T003", "잘못된 토큰 서명입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "T004", "리프레시 토큰이 존재하지 않습니다."),

    // Registration
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "R001", "이미 사용중인 아이디입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "R002", "이미 사용중인 이메일입니다."),
    REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R003", "계정 등록 요청을 찾을 수 없습니다."),
    REGISTRATION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "R004", "이미 처리된 계정 등록 요청입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    CURRENT_PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "U002", "현재 비밀번호가 일치하지 않습니다."),
    NEW_PASSWORD_NOT_CHANGED(HttpStatus.BAD_REQUEST, "U003", "새로운 비밀빈호는 기존 비밀번호와 달라야 합니다."),
    DUPLICATED_USER(HttpStatus.CONFLICT, "U004", "이미 존재하는 사용자입니다."),

    // Ticket
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "TI001", "해당 티켓을 찾을 수 없습니다."),
    UNAUTHORIZED_TICKET_ACCESS(HttpStatus.FORBIDDEN, "TI002", "티켓에 대한 접근 권한이 없습니다."),
    INVALID_TICKET_MANAGER(HttpStatus.BAD_REQUEST, "TI003", "유효하지 않은 담당자 ID입니다."),
    INVALID_EDIT_VALUE(HttpStatus.BAD_REQUEST,"TI004","사용자는 우선순위와 담당자를 수정할수없습니다."),
    UNAUTHORIZED_TICKET_EDIT(HttpStatus.FORBIDDEN, "TI005", "티켓상태 수정에 대한 접근 권한이 없습니다."),
    TICKET_REVIEW_NOT_REQUIRED(HttpStatus.BAD_REQUEST, "TI006", "검토를 요청하지 않은 티켓입니다."),
    TICKET_REVIEW_ALREADY_DONE(HttpStatus.BAD_REQUEST, "TI007", "이미 검토한 티켓입니다."),
    INVALID_TICKET_DEADLINE(HttpStatus.BAD_REQUEST,"TI008","마감기한이 유효하지않습니다."),

    // Subtask
    SUBTASK_NOT_FOUND(HttpStatus.NOT_FOUND,"S001","해당 하위태스크를 찾을수없습니다"),
    UNAUTHORIZED_SUBTASK_ACCESS(HttpStatus.FORBIDDEN,"S002","하위태스크에 대한 권한이 없습니다"),

    // Ticket Type
    TICKET_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "TT001", "티켓 유형을 찾을 수 없습니다."),
    DUPLICATED_TICKET_TYPE(HttpStatus.CONFLICT, "TT002", "이미 존재하는 티켓 유형입니다."),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT001", "카테고리를 찾을 수 없습니다."),
    DUPLICATED_CATEGORY_NAME(HttpStatus.CONFLICT, "CAT002", "이미 존재하는 카테고리 이름입니다."),
    PRIMARY_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CAT003", "유효한 1차 카테고리를 찾을 수 없습니다."),
    INVALID_CATEGORY_LEVEL(HttpStatus.BAD_REQUEST, "C004", "잘못된 카테고리 레벨입니다."),

    // Ticket Form
    TICKET_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "TF001", "티켓 폼을 찾을 수 없습니다."),
    DUPLICATED_TICKET_FORM(HttpStatus.CONFLICT, "TF002", "이미 존재하는 티켓 폼입니다."),

    // Ticket Comment
    TICKET_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TC001", "댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_TICKET_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "TC002", "댓글에 대한 접근 권한이 없습니다."),
    INVALID_TICKET_COMMENT(HttpStatus.BAD_REQUEST, "TC003", "유효하지 않은 댓글입니다."),

    // Ticket Template
    TICKET_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TPL001", "티켓 템플릿을 찾을 수 없습니다."),
    TICKET_TEMPLATE_INVALID_FK(HttpStatus.BAD_REQUEST, "TPL002", "티켓 템플릿 FK값이 유효하지 않습니다."),

    // Notification
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "N001", "유효하지 않은 알림 유형입니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N002", "알림을 찾을 수 없습니다."),

    // KakaoWork
    FETCHING_USER_ID_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K001", "카카오워크 사용자 정보를 가져올 수 없습니다."),
    FETCHING_CONVERSATION_ID_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K002", "카카오워크 채팅방을 열 수 없습니다."),
    SENDING_MESSAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K003", "카카오워크 메시지를 보내는 중 오류가 발생했습니다."),

    // Object Storage
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "O001", "최대 파일 크기는 10MB 입니다."),
    TOO_MANY_FILES(HttpStatus.BAD_REQUEST, "O002", "파일은 최대 5개까지 첨부할 수 있습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O003", "파일 업로드 중 오류가 발생했습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "O004", "유효하지 않은 파일명입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "O005", "허용되지 않는 파일 확장자입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "O006", "파일을 찾을 수 없습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O007", "파일 삭제 중 오류가 발생했습니다."),
    UNAUTHORIZED_FILE_ACCESS(HttpStatus.FORBIDDEN, "O008", "파일에 대한 접근 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
