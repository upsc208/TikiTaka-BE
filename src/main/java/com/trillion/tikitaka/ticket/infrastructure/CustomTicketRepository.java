package com.trillion.tikitaka.ticket.infrastructure;

import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomTicketRepository {
    TicketCountByStatusResponse countTicketsByStatus(Long requesterId, String role);
    Page<TicketListResponse> getTicketList(Pageable pageable, Ticket.Status status, Long firstCategoryId, Long secondCategoryId,
                                           Long ticketTypeId, Long managerId, Long requesterId, String role);
    TicketResponse getTicket(Long ticketId, Long userId, String role);
}
