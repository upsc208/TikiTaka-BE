package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketReviewNotRequiredException extends CustomException{
    public TicketReviewNotRequiredException(){super(ErrorCode.TICKET_REVIEW_NOT_REQUIRED);}
}
