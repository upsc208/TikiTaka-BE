package com.trillion.tikitaka.ticket.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.trillion.tikitaka.ticket.domain.QTicket.ticket;

@RequiredArgsConstructor
public class CustomTicketRepositoryImpl implements CustomTicketRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public TicketCountByStatusResponse countTicketsByStatus(Long requesterId, String role) {

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

        BooleanExpression conditions = buildRoleConditions(requesterId, role);

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

    @Override
    public Page<TicketListResponse> getTicketList(Pageable pageable, Ticket.Status status, Long firstCategoryId, Long secondCategoryId,
                                           Long ticketTypeId, Long managerId, Long requesterId, String role) {

        List<TicketListResponse> content = queryFactory
                .select(new QTicketListResponse(
                        ticket.id.as("ticketId"),
                        ticket.title,
                        ticket.description,
                        ticket.ticketType.name.as("typeName"),
                        ticket.firstCategory.name.as("firstCategoryName"),
                        ticket.secondCategory.name.as("secondCategoryName"),
                        ticket.manager.username.as("managerName"),
                        ticket.status,
                        ticket.urgent,
                        ticket.deadline
                ))
                .from(ticket)
                .leftJoin(ticket.ticketType)
                .leftJoin(ticket.firstCategory)
                .leftJoin(ticket.secondCategory)
                .leftJoin(ticket.manager)
                .where(
                        buildRoleConditions(requesterId, role),
                        ticketTypeEq(ticketTypeId),
                        firstCategoryEq(firstCategoryId),
                        secondCategoryEq(secondCategoryId),
                        managerEq(managerId),
                        statusEq(status),
                        deletedAtEqNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(ticket.count())
                .from(ticket)
                .leftJoin(ticket.ticketType)
                .leftJoin(ticket.firstCategory)
                .leftJoin(ticket.secondCategory)
                .leftJoin(ticket.manager)
                .where(
                        buildRoleConditions(requesterId, role),
                        ticketTypeEq(ticketTypeId),
                        firstCategoryEq(firstCategoryId),
                        secondCategoryEq(secondCategoryId),
                        managerEq(managerId),
                        statusEq(status),
                        deletedAtEqNull()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public TicketResponse getTicket(Long ticketId, Long userId, String role) {
        return queryFactory
                .select(new QTicketResponse(
                        ticket.id.as("ticketId"),
                        ticket.title,
                        ticket.description,
                        ticket.priority,
                        ticket.status,
                        ticket.ticketType.name.as("typeName"),
                        ticket.firstCategory.name.as("firstCategoryName"),
                        ticket.secondCategory.name.as("secondCategoryName"),
                        ticket.manager.username.as("managerName"),
                        ticket.requester.username.as("requesterName"),
                        ticket.urgent,
                        ticket.deadline,
                        ticket.createdAt,
                        ticket.updatedAt,
                        ticket.progress
                ))
                .from(ticket)
                .leftJoin(ticket.ticketType)
                .leftJoin(ticket.firstCategory)
                .leftJoin(ticket.secondCategory)
                .leftJoin(ticket.manager)
                .leftJoin(ticket.requester)
                .where(
                        buildRoleConditions(userId, role),
                        ticketIdEq(ticketId),
                        deletedAtEqNull()
                )
                .fetchOne();
    }

    private static BooleanExpression ticketIdEq(Long ticketId) {
        return ticket.id.eq(ticketId);
    }

    private BooleanExpression buildRoleConditions(Long requesterId, String role) {
        if ("USER".equals(role)) {
            return ticket.requester.id.eq(requesterId);
        }

        return Expressions.TRUE;
    }

    private BooleanExpression ticketTypeEq(Long ticketTypeId) {
        return ticketTypeId != null ? ticket.ticketType.id.eq(ticketTypeId) : null;
    }

    private BooleanExpression firstCategoryEq(Long firstCategoryId) {
        return firstCategoryId != null ? ticket.firstCategory.id.eq(firstCategoryId) : null;
    }

    private BooleanExpression secondCategoryEq(Long secondCategoryId) {
        return secondCategoryId != null ? ticket.secondCategory.id.eq(secondCategoryId) : null;
    }

    private BooleanExpression managerEq(Long managerId) {
        return managerId != null ? ticket.manager.id.eq(managerId) : null;
    }

    private BooleanExpression statusEq(Ticket.Status status) {
        return status != null ? ticket.status.eq(status) : null;
    }

    private static BooleanExpression deletedAtEqNull() {
        return ticket.deletedAt.isNull();
    }
}
