package com.trillion.tikitaka.registration.exception;

import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.exception.CustomException;

public class DuplicateUsernameException extends CustomException {
    public DuplicateUsernameException() {
        super(ErrorCode.DUPLICATED_USERNAME);
    }
}
