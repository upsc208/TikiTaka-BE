package com.trillion.tikitaka.tickettemplate.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.ticket.dto.response.QTicketListResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.QTicketTemplateListResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateListResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.tickettemplate.domain.QTicketTemplate.ticketTemplate;

@RequiredArgsConstructor
public class CustomTicketTemplateRepositoryImpl implements CustomTicketTemplateRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TicketTemplateListResponse> getAllTemplates(Long userId) {
        return queryFactory
                .select(new QTicketTemplateListResponse(
                        ticketTemplate.id.as("templateId"),
                        ticketTemplate.title.as("templateTitle"),
                        ticketTemplate.title,
                        ticketTemplate.type.id.as("typeId"),
                        ticketTemplate.type.name.as("typeName"),
                        ticketTemplate.firstCategory.id.as("firstCategoryId"),
                        ticketTemplate.firstCategory.name.as("firstCategoryName"),
                        ticketTemplate.secondCategory.id.as("secondCategoryId"),
                        ticketTemplate.secondCategory.name.as("secondCategoryName"),
                        ticketTemplate.createdAt,
                        ticketTemplate.updatedAt
                ))
                .from(ticketTemplate)
                .where(ticketTemplate.requester.id.eq(userId))
                .fetch();
    }
}
