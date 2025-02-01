package com.trillion.tikitaka.ticketcomment.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class UnauthorizedTicketCommentException extends CustomException {
    public UnauthorizedTicketCommentException(){super(ErrorCode.UNAUTHORIZED_TICKET_COMMENT_ACCESS);}
}
