package com.trillion.tikitaka.history.application;

import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.history.infrastructure.CustomHistoryRepository;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    public Page<HistoryResponse> getHistory(Pageable pageable, Long updatedById, Long ticketId, String updateType) {
        return historyRepository.getHistory(pageable, updatedById, ticketId, updateType);
    }

    @Transactional
    public void recordHistory(Ticket ticket, User user, TicketHistory.UpdateType updateType){
       TicketHistory history = TicketHistory.createHistory(ticket,user,updateType);
        historyRepository.save(history);
    }

}
