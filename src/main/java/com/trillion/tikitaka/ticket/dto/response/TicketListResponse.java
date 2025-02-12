package com.trillion.tikitaka.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.ticket.domain.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketListResponse {
    private Long ticketId;
    private String title;
    private String description;
    private String typeName;
    private String firstCategoryName;
    private String secondCategoryName;
    private String managerName;
    private Ticket.Status status;
    private Boolean urgent;
    private Ticket.Priority priority;
    private Double progress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @QueryProjection
    public TicketListResponse(Long ticketId, String title, String description, String typeName, String firstCategoryName,
                              String secondCategoryName, String managerName, Ticket.Status status, Boolean urgent,
                              Ticket.Priority priority, LocalDateTime deadline, LocalDateTime createdAt,Double progress) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.typeName = typeName;
        this.firstCategoryName = firstCategoryName;
        this.secondCategoryName = secondCategoryName;
        this.managerName = managerName;
        this.status = status;
        this.urgent = urgent;
        this.priority = priority;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.progress = progress;
    }
}
