package com.trillion.tikitaka.attachment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class FileSizeExceededException extends CustomException {

    public FileSizeExceededException(){super(ErrorCode.FILE_SIZE_EXCEEDED);}
}
