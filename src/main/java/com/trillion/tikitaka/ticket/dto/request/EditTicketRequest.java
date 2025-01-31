package com.trillion.tikitaka.ticket.dto.request;

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
