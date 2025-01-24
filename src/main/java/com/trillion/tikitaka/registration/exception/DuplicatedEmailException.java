package com.trillion.tikitaka.registration.exception;

import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.exception.CustomException;

public class DuplicatedEmailException extends CustomException {
    public DuplicatedEmailException() {
        super(ErrorCode.DUPLICATED_EMAIL);
    }
}
