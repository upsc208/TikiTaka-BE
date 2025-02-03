package com.trillion.tikitaka.tickettemplate.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketTemplateNotFoundException extends CustomException {
    public TicketTemplateNotFoundException() {
        super(ErrorCode.TICKET_TEMPLATE_NOT_FOUND);
    }
}
