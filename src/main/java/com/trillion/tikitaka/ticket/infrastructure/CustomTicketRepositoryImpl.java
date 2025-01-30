package com.trillion.tikitaka.ticket.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.QTicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import lombok.RequiredArgsConstructor;

import static com.trillion.tikitaka.ticket.domain.QTicket.ticket;

@RequiredArgsConstructor
public class CustomTicketRepositoryImpl implements CustomTicketRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public TicketCountByStatusResponse countTicketsByStatus(Boolean isUser, Long requesterId) {

        NumberExpression<Long> pending = new CaseBuilder()
                .when(ticket.status.eq(Ticket.Status.PENDING)).then(1L)
                .otherwise(0L);

        NumberExpression<Long> inProgress = new CaseBuilder()
                .when(ticket.status.eq(Ticket.Status.IN_PROGRESS)).then(1L)
                .otherwise(0L);

        NumberExpression<Long> reviewing = new CaseBuilder()
                .when(ticket.status.eq(Ticket.Status.REVIEW)).then(1L)
                .otherwise(0L);

        NumberExpression<Long> done = new CaseBuilder()
                .when(ticket.status.eq(Ticket.Status.DONE)).then(1L)
                .otherwise(0L);

        NumberExpression<Long> urgent = new CaseBuilder()
                .when(ticket.urgent.eq(true)).then(1L)
                .otherwise(0L);

        BooleanExpression conditions = buildRoleConditions(isUser, requesterId);

        return queryFactory
                .select(new QTicketCountByStatusResponse(
                        ticket.count().as("total"),
                        pending.sum().as("pending"),
                        inProgress.sum().as("inProgress"),
                        reviewing.sum().as("reviewing"),
                        done.sum().as("completed"),
                        urgent.sum().as("urgent")
                ))
                .from(ticket)
                .where(conditions)
                .fetchOne();
    }

    private BooleanExpression buildRoleConditions(Boolean isUser, Long requesterId) {
        if (Boolean.TRUE.equals(isUser) && requesterId != null) {
            return ticket.requester.id.eq(requesterId);
        }
        return null;
    }
}
