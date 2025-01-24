package com.trillion.tikitaka.tickettype.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.tickettype.dto.response.QTicketTypeListResponse;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.tickettype.domain.QTicketType.ticketType;

@RequiredArgsConstructor
public class CustomTicketTypeRepositoryImpl implements CustomTicketTypeRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TicketTypeListResponse> getTicketTypes(Boolean active) {

        return queryFactory
                .select(new QTicketTypeListResponse(
                        ticketType.id.as("typeId"),
                        ticketType.name.as("typeName")
                ))
                .from(ticketType)
                .where(activeEq(active))
                .fetch();
    }

    private BooleanExpression activeEq(Boolean active) {
        if (active == null) return null;
        return active ? ticketType.active.isTrue() : ticketType.active.isFalse();
    }
}
