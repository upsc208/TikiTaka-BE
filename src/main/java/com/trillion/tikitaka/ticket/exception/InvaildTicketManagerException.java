package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvaildTicketManagerException extends CustomException {
    public InvaildTicketManagerException(){super(ErrorCode.INVALID_TICKET_MANAGER);}
}
