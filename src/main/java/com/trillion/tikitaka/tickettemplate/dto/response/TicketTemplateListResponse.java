package com.trillion.tikitaka.tickettemplate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TicketTemplateListResponse {
    private Long templateId;
    private String templateTitle;
    private String title;
    private Long typeId;
    private String typeName;
    private Long firstCategoryId;
    private String firstCategoryName;
    private Long secondCategoryId;
    private String secondCategoryName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @QueryProjection
    public TicketTemplateListResponse(Long templateId, String templateTitle, String title, Long typeId, String typeName,
                                      Long firstCategoryId, String firstCategoryName, Long secondCategoryId, String secondCategoryName,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.templateId = templateId;
        this.templateTitle = templateTitle;
        this.title = title;
        this.typeId = typeId;
        this.typeName = typeName;
        this.firstCategoryId = firstCategoryId;
        this.firstCategoryName = firstCategoryName;
        this.secondCategoryId = secondCategoryId;
        this.secondCategoryName = secondCategoryName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
