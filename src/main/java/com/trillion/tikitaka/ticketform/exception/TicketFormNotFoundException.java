package com.trillion.tikitaka.ticketform.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketFormNotFoundException extends CustomException {
    public TicketFormNotFoundException() {
        super(ErrorCode.TICKET_FORM_NOT_FOUND);
    }
}
