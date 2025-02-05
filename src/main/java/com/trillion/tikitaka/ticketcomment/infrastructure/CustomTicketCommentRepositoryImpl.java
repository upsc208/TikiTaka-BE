package com.trillion.tikitaka.ticketcomment.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.attachment.dto.response.AttachmentResponse;
import com.trillion.tikitaka.attachment.dto.response.QAttachmentResponse;
import com.trillion.tikitaka.ticketcomment.dto.response.QTicketCommentResponse;
import com.trillion.tikitaka.ticketcomment.dto.response.TicketCommentResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.attachment.domain.QAttachment.attachment;
import static com.trillion.tikitaka.ticketcomment.domain.QTicketComment.ticketComment;

@RequiredArgsConstructor
public class CustomTicketCommentRepositoryImpl implements CustomTicketCommentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TicketCommentResponse> getTicketComments(Long ticketId) {
        List<TicketCommentResponse> comments = queryFactory
                .select(new QTicketCommentResponse(
                        ticketComment.id.as("commentId"),
                        ticketComment.author.id.as("authorId"),
                        ticketComment.author.username.as("authorName"),
                        ticketComment.content,
                        ticketComment.createdAt,
                        ticketComment.updatedAt
                ))
                .from(ticketComment)
                .where(ticketIdEq(ticketId))
                .fetch();

        for (TicketCommentResponse comment : comments) {
            List<AttachmentResponse> attachments = queryFactory
                    .select(new QAttachmentResponse(
                            attachment.id.as("attachmentId"),
                            attachment.fileName,
                            attachment.filePath,
                            attachment.fileSize
                    ))
                    .from(attachment)
                    .where(attachmentCommentIdEq(comment))
                    .fetch();
            comment.setAttachments(attachments);
        }

        return comments;
    }

    private static BooleanExpression attachmentCommentIdEq(TicketCommentResponse comment) {
        return attachment.comment.id.eq(comment.getCommentId());
    }

    private static BooleanExpression ticketIdEq(Long ticketId) {
        return ticketComment.ticket.id.eq(ticketId);
    }
}
