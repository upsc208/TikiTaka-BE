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
    private String typeName;

    private Long firstCategoryId;
    private String firstCategoryName;

    private Long secondCategoryId;
    private String secondCategoryName;

    private Long requesterId;
    private String requesterName;

    private Long managerId;
    private String managerName;
}
