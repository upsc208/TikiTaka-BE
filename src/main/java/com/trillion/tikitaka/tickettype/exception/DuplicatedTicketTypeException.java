package com.trillion.tikitaka.tickettype.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class DuplicatedTicketTypeException extends CustomException {
    public DuplicatedTicketTypeException() {
        super(ErrorCode.DUPLICATED_TICKET_TYPE);
    }
}
