package com.trillion.tikitaka.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data;

    public ApiResponse(T data) {
        this.message = "요청이 성공적으로 처리되었습니다";
        this.data = data;
    }
}
