package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class UnauthorizedTicketEditExeception extends CustomException {
    public UnauthorizedTicketEditExeception(){super(ErrorCode.UNAUTHORIZED_TICKET_EDIT);}
}
