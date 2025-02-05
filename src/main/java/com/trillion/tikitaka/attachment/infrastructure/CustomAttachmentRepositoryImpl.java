package com.trillion.tikitaka.attachment.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.attachment.dto.response.AttachmentResponse;
import com.trillion.tikitaka.attachment.dto.response.QAttachmentResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.attachment.domain.QAttachment.attachment;

@RequiredArgsConstructor
public class CustomAttachmentRepositoryImpl implements CustomAttachmentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AttachmentResponse> getTicketAttachments(Long ticketId) {
        return queryFactory
                .select(new QAttachmentResponse(
                        attachment.id.as("attachmentId"),
                        attachment.fileName,
                        attachment.filePath,
                        attachment.fileSize
                ))
                .from(attachment)
                .where(ticketIdEq(ticketId))
                .fetch();
    }

    private static BooleanExpression ticketIdEq(Long ticketId) {
        return attachment.ticket.id.eq(ticketId);
    }
}
