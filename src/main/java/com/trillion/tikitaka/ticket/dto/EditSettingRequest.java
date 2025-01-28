package com.trillion.tikitaka.ticket.dto;

import com.trillion.tikitaka.ticket.domain.Ticket;
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

    private LocalDateTime deadline;
}
