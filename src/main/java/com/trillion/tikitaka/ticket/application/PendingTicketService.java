package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.ticket.dto.response.PendingTicketResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingTicketService {

    private final TicketRepository ticketRepository;

    public PendingTicketResponse getPendingTickets(Long managerId) {
        // 담당자가 본인이고 PENDING 상태인 티켓 수
        int myPendingTicket = ticketRepository.countByManagerAndStatus(managerId, Ticket.Status.PENDING);

        // 담당자가 지정되지 않고 PENDING 상태인 티켓 수
        int unassignedPendingTicket = ticketRepository.countByManagerIsNullAndStatus(Ticket.Status.PENDING);

        // 총 대기 티켓 수 (내 요청 + 그룹 요청)
        int totalPendingTicket = myPendingTicket + unassignedPendingTicket;

        // 긴급 대기 티켓 수 (담당자가 본인 or 지정되지 않고 PENDING & URGENT)
        int urgentPendingTicket = ticketRepository.countUrgentPendingTickets(managerId, Ticket.Status.PENDING);

        return new PendingTicketResponse(myPendingTicket, unassignedPendingTicket, totalPendingTicket, urgentPendingTicket);
    }
}
