package com.trillion.tikitaka.attachment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidFileNameException extends CustomException {

    public InvalidFileNameException(){super(ErrorCode.INVALID_FILE_NAME);}
}
