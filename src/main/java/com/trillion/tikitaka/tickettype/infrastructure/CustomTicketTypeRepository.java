package com.trillion.tikitaka.tickettype.infrastructure;

import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;

import java.util.List;

public interface CustomTicketTypeRepository {
    List<TicketTypeListResponse> getTicketTypes(Boolean active);

    List<TicketType> findByIdAndNameCheck(Long id, String name);
}
