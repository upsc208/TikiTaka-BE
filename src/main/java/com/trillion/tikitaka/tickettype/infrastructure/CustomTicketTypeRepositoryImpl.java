package com.trillion.tikitaka.tickettype.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.tickettype.domain.TicketType;
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

    @Override
    public List<TicketType> findByIdAndNameCheck(Long id, String name) {
        return queryFactory
                .selectFrom(ticketType)
                .where(
                        typeIdEq(id)
                        .or(typeNameEq(name))
                )
                .fetch();
    }

    private static BooleanExpression typeIdEq(Long id) {
        return ticketType.id.eq(id);
    }

    private static BooleanExpression typeNameEq(String name) {
        return ticketType.name.eq(name);
    }

    private BooleanExpression activeEq(Boolean active) {
        if (active == null) return null;
        return active ? ticketType.active.isTrue() : ticketType.active.isFalse();
    }
}
