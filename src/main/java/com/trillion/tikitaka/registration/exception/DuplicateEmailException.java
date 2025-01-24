package com.trillion.tikitaka.registration.exception;

import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.exception.CustomException;

public class DuplicateEmailException extends CustomException {
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATED_EMAIL);
    }
}
