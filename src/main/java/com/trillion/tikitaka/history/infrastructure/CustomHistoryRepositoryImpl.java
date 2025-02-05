package com.trillion.tikitaka.history.infrastructure;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.QTicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.trillion.tikitaka.ticket.domain.QTicket.ticket;

@RequiredArgsConstructor
public class CustomHistoryRepositoryImpl implements CustomHistoryRepository {

    private final JPAQueryFactory queryFactory;

    
}
