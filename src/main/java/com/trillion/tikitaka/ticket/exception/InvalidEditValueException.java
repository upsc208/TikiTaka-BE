package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidEditValueException extends CustomException{
    public InvalidEditValueException(){super(ErrorCode.INVALID_EDIT_VALUE);}
}
