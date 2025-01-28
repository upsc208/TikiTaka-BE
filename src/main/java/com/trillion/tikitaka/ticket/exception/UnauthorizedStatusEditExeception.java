package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class UnauthorizedStatusEditExeception extends CustomException {
    public UnauthorizedStatusEditExeception(){super(ErrorCode.UNAUTHORIZED_STATUS_EDIT);}
}
