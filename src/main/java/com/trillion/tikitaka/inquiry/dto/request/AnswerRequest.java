package com.trillion.tikitaka.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnswerRequest {

    @NotBlank(message = "답변 내용은 공백일 수 없습니다.")
    private String answer;
}
