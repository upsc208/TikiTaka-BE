package com.trillion.tikitaka.ticket.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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

        private Boolean urgent;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime deadline;

}
