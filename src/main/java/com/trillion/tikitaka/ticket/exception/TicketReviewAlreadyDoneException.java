package com.trillion.tikitaka.ticket.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class TicketReviewAlreadyDoneException extends CustomException{
    public TicketReviewAlreadyDoneException(){super(ErrorCode.TICKET_REVIEW_ALREADY_DONE);}
}
