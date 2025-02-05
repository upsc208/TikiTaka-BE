package com.trillion.tikitaka.history.application;

import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    @Transactional
    public void recordHistory(Ticket ticket, User user, TicketHistory.UpdateType updateType){
       TicketHistory history = TicketHistory.createHistory(ticket,user,updateType);
        historyRepository.save(history);
    }

    public Page getHistory(){
        return null;
    }
}
