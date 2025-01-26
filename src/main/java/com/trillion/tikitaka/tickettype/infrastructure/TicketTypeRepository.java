package com.trillion.tikitaka.tickettype.infrastructure;

import com.trillion.tikitaka.tickettype.domain.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long>, CustomTicketTypeRepository {
    Optional<TicketType> findByName(String name);
}
