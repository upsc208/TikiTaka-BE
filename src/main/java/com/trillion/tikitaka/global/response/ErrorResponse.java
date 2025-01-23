package com.trillion.tikitaka.global.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private HttpStatus httpStatus;
    private String errorCode;
    private String message;
}
