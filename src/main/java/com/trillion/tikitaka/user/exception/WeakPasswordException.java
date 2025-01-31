package com.trillion.tikitaka.user.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class WeakPasswordException extends CustomException {
    public WeakPasswordException() {
        super(ErrorCode.WEAK_PASSWORD);
    }
}
