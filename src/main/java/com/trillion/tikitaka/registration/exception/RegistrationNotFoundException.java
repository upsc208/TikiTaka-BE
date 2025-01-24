package com.trillion.tikitaka.registration.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class RegistrationNotFoundException extends CustomException {
    public RegistrationNotFoundException() {
        super(ErrorCode.REGISTRATION_NOT_FOUND);
    }
}
