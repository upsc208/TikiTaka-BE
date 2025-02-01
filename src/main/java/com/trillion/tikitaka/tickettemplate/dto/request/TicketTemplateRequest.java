package com.trillion.tikitaka.tickettemplate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * managerId 제외 전부 필수
 * typeId, firstCategoryId, secondCategoryId, requesterId -> 유효한 FK인지 체크
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketTemplateRequest {
    private String templateTitle;
    private String title;
    private String description;
    private Long typeId;
    private Long firstCategoryId;
    private Long secondCategoryId;
    private Long requesterId;   // 반드시 존재하는 user
    private Long managerId;     // optional
    private String createdAt;   // e.g. "2025-02-19"
}
