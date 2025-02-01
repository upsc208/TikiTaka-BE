package com.trillion.tikitaka.ticketcomment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TicketCommentResponse {
    private Long commentId;
    private Long authorId;
    private String authorName;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @QueryProjection
    public TicketCommentResponse(Long commentId, Long authorId, String authorName, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
