package com.trillion.tikitaka.user.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class SamePasswordException extends CustomException {
    public SamePasswordException() {
        super(ErrorCode.PASSWORD_SAME_AS_OLD);
    }
}
