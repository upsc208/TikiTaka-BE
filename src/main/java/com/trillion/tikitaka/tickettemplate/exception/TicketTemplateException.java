package com.trillion.tikitaka.tickettemplate.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketTemplateException extends CustomException {
    public TicketTemplateException(ErrorCode errorCode) {
        super(errorCode);
    }
}
