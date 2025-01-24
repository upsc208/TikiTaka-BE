package com.trillion.tikitaka.registration.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class RegistrationAlreadyProcessedException extends CustomException {
    public RegistrationAlreadyProcessedException() {
        super(ErrorCode.REGISTRATION_ALREADY_PROCESSED);
    }
}
