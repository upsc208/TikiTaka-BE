package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidTicektDeadlineException extends CustomException {
    public InvalidTicektDeadlineException(){super(ErrorCode.INVALID_TICKET_DEADLINE);}
}
