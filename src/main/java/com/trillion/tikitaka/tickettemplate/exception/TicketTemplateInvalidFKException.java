package com.trillion.tikitaka.tickettemplate.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketTemplateInvalidFKException extends CustomException {
    public TicketTemplateInvalidFKException() {
        super(ErrorCode.TICKET_TEMPLATE_INVALID_FK);
    }
}
