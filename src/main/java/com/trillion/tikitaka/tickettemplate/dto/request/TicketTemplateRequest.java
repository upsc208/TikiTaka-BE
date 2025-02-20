package com.trillion.tikitaka.tickettemplate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketTemplateRequest {

    @NotBlank(message = "템플릿 제목을 입력해주세요.")
    @Size(max = 150, message = "템플릿 제목은 150자를 초과할 수 없습니다.")
    private String templateTitle;

    @NotBlank(message = "티켓 제목을 입력해주세요.")
    @Size(max = 150, message = "티켓 제목은 150자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "티켓 내용을 입력해주세요.")
    @Size(max = 5000, message = "티켓 내용은 5000자를 초과할 수 없습니다.")
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