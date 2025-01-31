package com.trillion.tikitaka.ticketform.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class DuplicatedTicketFormException extends CustomException {
    public DuplicatedTicketFormException() {
        super(ErrorCode.DUPLICATED_TICKET_FORM);
    }
}
