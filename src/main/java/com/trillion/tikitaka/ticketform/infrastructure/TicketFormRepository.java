package com.trillion.tikitaka.ticketform.infrastructure;

import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketFormRepository extends JpaRepository<TicketForm, TicketFormId> {
}
