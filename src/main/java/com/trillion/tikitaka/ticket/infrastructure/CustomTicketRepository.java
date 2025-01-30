package com.trillion.tikitaka.ticket.infrastructure;

import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;

public interface CustomTicketRepository {
    TicketCountByStatusResponse countTicketsByStatus(Boolean isUser, Long requesterId);
}
