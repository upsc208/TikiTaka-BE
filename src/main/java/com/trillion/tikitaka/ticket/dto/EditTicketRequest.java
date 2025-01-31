package com.trillion.tikitaka.ticket.dto;

import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditTicketRequest {

        private String title;

        private String description;

        private Long ticketTypeId;

        private Long firstCategoryId;

        private Long secondCategoryId;

        private Long requesterId;

        private Boolean urgent;

}
