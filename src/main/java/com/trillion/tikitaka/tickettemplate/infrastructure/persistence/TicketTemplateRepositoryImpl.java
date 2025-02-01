package com.trillion.tikitaka.tickettemplate.infrastructure.persistence;

import com.trillion.tikitaka.tickettemplate.domain.model.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.domain.repository.TicketTemplateRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTemplateRepositoryImpl
        extends JpaRepository<TicketTemplate, Long>, TicketTemplateRepository {


}
