package com.trillion.tikitaka.history.application;

import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.history.infrastructure.CustomHistoryRepository;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Page<HistoryResponse> getHistory(Pageable pageable, Long updatedById, Long ticketId, String updateType) {
        log.info("[티켓 이력 조회] 티켓 ID: {}, 수정 유형: {}, 수정자: {}", ticketId, updateType, updatedById);
        if (!ticketRepository.existsById(ticketId)) {
            throw new TicketNotFoundException();
        }
        return historyRepository.getHistory(pageable, updatedById, ticketId, updateType);
    }

    @Transactional
    public void recordHistory(Ticket ticket, User user, TicketHistory.UpdateType updateType) {
        log.info("[티켓 이력 기록] 티켓 ID: {}, 수정 유형: {}, 수정자: {}", ticket.getId(), updateType, user.getId());
       TicketHistory history = TicketHistory.createHistory(ticket,user,updateType);
        historyRepository.save(history);
    }

}
