package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketNotFoundException extends CustomException {
    public TicketNotFoundException(){super(ErrorCode.TICKET_NOT_FOUND);}
}
