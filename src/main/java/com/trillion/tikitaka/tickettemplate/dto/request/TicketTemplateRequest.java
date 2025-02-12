package com.trillion.tikitaka.tickettemplate.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
public class TicketTemplateRequest {
    @NotBlank
    private String templateTitle;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private Long typeId;
    private Long firstCategoryId;
    private Long secondCategoryId;

    private Long managerId;
    public TicketTemplateRequest(Long typeId, Long firstCategoryId, Long secondCategoryId, Long managerId,
                                 String templateTitle, String title, String description) {
        this.typeId = typeId;
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
        this.managerId = managerId;
        this.templateTitle = templateTitle;
        this.title = title;
        this.description = description;
    }

}