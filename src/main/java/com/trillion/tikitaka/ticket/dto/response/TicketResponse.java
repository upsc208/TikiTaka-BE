package com.trillion.tikitaka.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.attachment.dto.response.AttachmentResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TicketResponse {
    private Long ticketId;
    private String title;
    private String description;
    private Ticket.Priority priority;
    private Ticket.Status status;
    private Long typeId;
    private String typeName;
    private Long firstCategoryId;
    private String firstCategoryName;
    private Long secondCategoryId;
    private String secondCategoryName;
    private Long managerId;
    private String managerName;
    private Long requesterId;
    private String requesterName;
    private Boolean urgent;
    private Double progress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;

    private List<AttachmentResponse> attachments;

    @QueryProjection
    public TicketResponse(Long ticketId, String title, String description, Ticket.Priority priority, Ticket.Status status,
                          Long typeId, String typeName, Long firstCategoryId, String firstCategoryName, Long secondCategoryId,
                          String secondCategoryName, Long managerId, String managerName, Long requesterId, String requesterName,
                          Boolean urgent, LocalDateTime deadline, LocalDateTime createdAt, LocalDateTime updatedAt,Double progress) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.typeId = typeId;
        this.typeName = typeName;
        this.firstCategoryId = firstCategoryId;
        this.firstCategoryName = firstCategoryName;
        this.secondCategoryId = secondCategoryId;
        this.secondCategoryName = secondCategoryName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.urgent = urgent;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.progress = progress;
    }
}
