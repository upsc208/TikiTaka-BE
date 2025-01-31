package com.trillion.tikitaka.ticketcomment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketCommentNotFoundException extends CustomException {
    public TicketCommentNotFoundException(){super(ErrorCode.TICKET_COMMENT_NOT_FOUND);}
}
