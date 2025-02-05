package com.trillion.tikitaka.tickettemplate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketTemplateListResponse {

    private Long id;
    private String templateTitle;
    private String title;

    private Long typeId;
    private String typeName;

    private Long firstCategoryId;
    private String firstCategoryName;

    private Long secondCategoryId;
    private String secondCategoryName;

    private String createdAt;
    private String updatedAt;
}
