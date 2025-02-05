package com.trillion.tikitaka.attachment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class FileNotFoundException extends CustomException {

    public FileNotFoundException(){super(ErrorCode.FILE_NOT_FOUND);}
}
