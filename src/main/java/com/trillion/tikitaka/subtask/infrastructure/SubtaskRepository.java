package com.trillion.tikitaka.subtask.infrastructure;

import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    List<Subtask> findByParentTicket(Ticket parentTicket);
    List<Subtask> findAllByParentTicket(Ticket ticket);

    Double countAllByParentTicketId(Long ticketId);

    Double countAllByDoneIsTrueAndParentTicketId(Long ticketId);
    ;
}
