package com.trillion.tikitaka.tickettemplate.infrastructure;

import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTemplateRepository extends JpaRepository<TicketTemplate, Long>, CustomTicketTemplateRepository {
}