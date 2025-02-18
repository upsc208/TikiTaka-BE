package com.trillion.tikitaka.history.infrastructure;

import com.trillion.tikitaka.history.domain.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<TicketHistory,Long> ,CustomHistoryRepository{
    List<TicketHistory> findByTicketId(Long ticketId);
}
