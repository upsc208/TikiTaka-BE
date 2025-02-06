package com.trillion.tikitaka.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingTicketResponse {
    private int myPendingTicket;  // 담당자가 본인이고 PENDING 상태인 티켓 수
    private int unassignedPendingTicket;  // 담당자가 지정되지 않고 PENDING 상태인 티켓 수
    private int totalPendingTicket;  // 내 요청 수 + 그룹 요청 수
    private int urgentPendingTicket;  // 담당자가 본인 or 지정되지 않고 긴급 + PENDING 상태인 티켓 수
}
