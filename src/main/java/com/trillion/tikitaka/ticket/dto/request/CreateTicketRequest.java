package com.trillion.tikitaka.ticket.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.trillion.tikitaka.global.jackson.CustomLocalDateTimeDeserializer;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    @NotNull(message = "상세 내용을 입력해주세요.")
    @Size(max = 5000, message = "상세 내용은 5000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "티켓 유형을 선택해주세요.")
    private Long typeId;

    private Long firstCategoryId;

    private Long secondCategoryId;

    @NotNull(message = "마감일을 입력해주세요.")
    @JsonSerialize(using = LocalDateTimeSerializer.class) //역직렬화,직렬화 대비
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;

    private Long managerId;

    @Builder.Default
    private Boolean urgent = false;
}
