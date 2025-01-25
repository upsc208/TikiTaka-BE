package com.trillion.tikitaka.tickettype.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketTypeNotFoundException extends CustomException {
    public TicketTypeNotFoundException() {
        super(ErrorCode.TICKET_TYPE_NOT_FOUND);
    }
}
