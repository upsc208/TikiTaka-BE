package com.trillion.tikitaka.ticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @Size(max = 5000, message = "상세 내용은 5000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "티켓 유형을 선택해주세요.")
    @Min(value = 1, message = "유효한 티켓 유형을 선택해주세요.")
    private Long typeId;

    @Min(value = 1, message = "유효한 카테고리를 선택해주세요.")
    private Long firstCategoryId;

    @Min(value = 1, message = "유효한 카테고리를 선택해주세요.")
    private Long secondCategoryId;

    @NotNull(message = "마감일을 선택해주세요.")
    private LocalDate deadline;

    private Long managerId;

    @Builder.Default
    private Boolean urgent = false;
}
