package com.trillion.tikitaka.ticket.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @Size(max = 5000, message = "상세 내용은 5000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "티켓 유형 ID는 필수 항목입니다.")
    private Long typeId;

    private Long firstCategoryId;

    private Long secondCategoryId;

    @NotNull(message = "마감일은 필수 항목입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;

    private Long managerId;

    @Builder.Default
    private Boolean urgent = false;


}

