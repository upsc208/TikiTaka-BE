package com.trillion.tikitaka.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingTicketResponse {
    private int myPendingTicket;
    private int allPendingTicket;
    private int urgentPendingTicket;
}
