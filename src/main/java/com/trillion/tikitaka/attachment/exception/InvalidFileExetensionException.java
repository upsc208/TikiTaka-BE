package com.trillion.tikitaka.attachment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidFileExetensionException extends CustomException {

    public InvalidFileExetensionException(){super(ErrorCode.INVALID_FILE_EXTENSION);}
}
