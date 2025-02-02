package com.trillion.tikitaka.tickettemplate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long typeId;

    @NotNull
    private Long firstCategoryId;

    @NotNull
    private Long secondCategoryId;

    @NotNull
    private Long requesterId;

    private Long managerId;
}
