package com.trillion.tikitaka.history.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.history.domain.QTicketHistory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.history.dto.response.QHistoryResponse;
import com.trillion.tikitaka.ticket.domain.QTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomHistoryRepositoryImpl implements CustomHistoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HistoryResponse> getHistory(Pageable pageable, Long updatedById, Long ticketId, String updateType) {
        QTicketHistory history = QTicketHistory.ticketHistory;
        QTicket ticket = QTicket.ticket;

        List<HistoryResponse> content = queryFactory
                .select(new QHistoryResponse(
                        history.id,
                        history.ticket.id,
                        ticket.title,
                        history.updatedBy.username,
                        history.updatedAt,
                        history.updateType
                ))
                .from(history)
                .leftJoin(history.ticket, ticket)
                .where(
                        updatedByEq(updatedById),
                        ticketIdEq(ticketId),
                        updateTypeEq(updateType)
                )
                .orderBy(history.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(history.count())
                .from(history)
                .leftJoin(history.ticket, ticket)
                .where(
                        updatedByEq(updatedById),
                        ticketIdEq(ticketId),
                        updateTypeEq(updateType)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression updatedByEq(Long updatedById) {
        QTicketHistory history = QTicketHistory.ticketHistory;
        return updatedById != null ? history.updatedBy.id.eq(updatedById) : null;
    }


    private BooleanExpression ticketIdEq(Long ticketId) {
        QTicketHistory history = QTicketHistory.ticketHistory;
        return ticketId != null ? history.ticket.id.eq(ticketId) : null;
    }


    private BooleanExpression updateTypeEq(String updateType) {
        QTicketHistory history = QTicketHistory.ticketHistory;

        if (updateType == null || updateType.isEmpty()) {
            return null;
        }

        return history.updateType.stringValue().eq(updateType);
    }
}
