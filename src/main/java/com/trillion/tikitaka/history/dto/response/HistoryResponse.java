package com.trillion.tikitaka.history.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.history.domain.TicketHistory.UpdateType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryResponse {
    private Long id;
    private Long ticketId;
    private String ticketTitle;
    private String updatedByUsername;
    private LocalDateTime updatedAt;
    private UpdateType updateType;

    @QueryProjection
    public HistoryResponse(Long id, Long ticketId, String ticketTitle, String updatedByUsername,
                           LocalDateTime updatedAt, UpdateType updateType) {
        this.id = id;
        this.ticketId = ticketId;
        this.ticketTitle = ticketTitle;
        this.updatedByUsername = updatedByUsername;
        this.updatedAt = updatedAt;
        this.updateType = updateType;
    }
}
