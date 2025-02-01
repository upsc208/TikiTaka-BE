package com.trillion.tikitaka.tickettemplate.domain.repository;

import com.trillion.tikitaka.tickettemplate.domain.model.TicketTemplate;

import java.util.Optional;

public interface TicketTemplateRepository {

    TicketTemplate save(TicketTemplate template);

    Optional<TicketTemplate> findById(Long id);
}
