package com.trillion.tikitaka.ticket.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditSettingRequest {

    private Long managerId;

    private Ticket.Priority priority;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    private TicketType type;

    private Ticket.Status status;
}
