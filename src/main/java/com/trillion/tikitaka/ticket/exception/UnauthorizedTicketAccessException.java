package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class UnauthorizedTicketAccessException extends CustomException {
    public UnauthorizedTicketAccessException(){super(ErrorCode.UNAUTHORIZED_TICKET_ACCESS);}
}
