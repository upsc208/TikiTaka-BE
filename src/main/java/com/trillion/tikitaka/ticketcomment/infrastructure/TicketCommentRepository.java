package com.trillion.tikitaka.ticketcomment.infrastructure;

import com.trillion.tikitaka.ticketcomment.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long>, CustomTicketCommentRepository {
}
