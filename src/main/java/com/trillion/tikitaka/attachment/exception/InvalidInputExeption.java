package com.trillion.tikitaka.attachment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidInputExeption extends CustomException {

    public InvalidInputExeption(){super(ErrorCode.INVALID_REQUEST_VALUE);}
}
