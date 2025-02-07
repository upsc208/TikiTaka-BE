package com.trillion.tikitaka.tickettemplate.infrastructure;

import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateListResponse;

import java.util.List;

public interface CustomTicketTemplateRepository {
    List<TicketTemplateListResponse> getAllTemplates(Long userId);
}
