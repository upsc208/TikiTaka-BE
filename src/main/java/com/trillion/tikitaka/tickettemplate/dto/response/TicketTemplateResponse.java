package com.trillion.tikitaka.tickettemplate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketTemplateResponse {
    private String templateTitle;
    private String title;
    private String description;
    private Long typeId;
    private Long firstCategoryId;
    private Long secondCategoryId;
    private Long requesterId;
    private Long managerId;
}
