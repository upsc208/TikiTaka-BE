package com.trillion.tikitaka.ticket.dto;

import com.trillion.tikitaka.ticket.domain.Ticket;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditTicketRequest {




        private String title; // 제목


        private String description; // 상세 내용


        private Ticket.Priority priority; // 우선순위

        private Ticket.Status status;

        private Long typeId; // 티켓 유형 ID

        private Long firstCategoryId; // 1차 카테고리 ID

        private Long secondCategoryId; // 2차 카테고리 ID


        private LocalDateTime deadline; // 마감일


        private Long requesterId; // 요청자 ID


        private Long managerId;

        private Boolean urgent = false; // 긴급 여부



}
