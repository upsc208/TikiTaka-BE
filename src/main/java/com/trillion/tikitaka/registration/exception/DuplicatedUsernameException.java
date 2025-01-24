package com.trillion.tikitaka.registration.exception;

import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.exception.CustomException;

public class DuplicatedUsernameException extends CustomException {
    public DuplicatedUsernameException() {
        super(ErrorCode.DUPLICATED_USERNAME);
    }
}
