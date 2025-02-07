package com.trillion.tikitaka.ticket.infrastructure;

import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketResponse;
import com.trillion.tikitaka.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomTicketRepository {
    TicketCountByStatusResponse countTicketsByStatus(Long requesterId, String role);

    Page<TicketListResponse> getTicketList(Pageable pageable, Ticket.Status status, Long firstCategoryId, Long secondCategoryId,
                                           Long ticketTypeId, Long managerId, Long requesterId, String role, String dateOption);

    TicketResponse getTicket(Long ticketId, Long userId, String role);

    List<Ticket> findUnassignedTickets(LocalDateTime createdBefore);

    Long countTicketsByManagerAndStatusIn(User manager, List<Ticket.Status> statuses);

    Long countByManagerAndTicketStatus(User manager, Ticket.Status status);
}
