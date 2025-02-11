package com.trillion.tikitaka.history.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.history.domain.TicketHistory.UpdateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HistoryResponse {
    private Long id;

    private Long ticketId;

    private String ticketTitle;

    private String updatedByUsername;

    @JsonSerialize(using = LocalDateTimeSerializer.class) //역직렬화,직렬화 대비
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
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
