package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidTicketManagerException extends CustomException {
    public InvalidTicketManagerException(){super(ErrorCode.INVALID_TICKET_MANAGER);}
}
