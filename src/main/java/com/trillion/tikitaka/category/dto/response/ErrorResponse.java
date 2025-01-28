package com.trillion.tikitaka.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String error; // 에러 메시지
    private int status;   // HTTP 상태 코드
}
