package com.trillion.tikitaka.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.ticket.domain.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TicketResponse {
    private Long ticketId;
    private String title;
    private String description;
    private Ticket.Priority priority;
    private Ticket.Status status;
    private String typeName;
    private String firstCategoryName;
    private String secondCategoryName;
    private String managerName;
    private String requesterName;
    private Boolean urgent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;

    @QueryProjection
    public TicketResponse(Long ticketId, String title, String description, Ticket.Priority priority, Ticket.Status status,
                          String typeName, String firstCategoryName, String secondCategoryName, String managerName,
                          String requesterName, Boolean urgent, LocalDateTime deadline, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.typeName = typeName;
        this.firstCategoryName = firstCategoryName;
        this.secondCategoryName = secondCategoryName;
        this.managerName = managerName;
        this.requesterName = requesterName;
        this.urgent = urgent;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
