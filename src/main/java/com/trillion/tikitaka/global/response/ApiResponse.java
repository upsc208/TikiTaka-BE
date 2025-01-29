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

    // 기본 생성자(위 @NoArgsConstructor) 말고, data 파라미터를 받는 생성자
    public ApiResponse(T data) {
        this.message = "요청이 성공적으로 처리되었습니다";
        this.data = data;
    }

    /**
     * Controller에서 쉽게 "ApiResponse.success(...)"로 쓸 수 있도록
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }
}
