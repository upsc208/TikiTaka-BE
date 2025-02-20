package com.trillion.tikitaka.ticketcomment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidTicketCommentException extends CustomException {
    public InvalidTicketCommentException(){super(ErrorCode.INVALID_TICKET_COMMENT);}
}
