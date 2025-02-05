package com.trillion.tikitaka.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryRequest {

    @NotBlank
    private String content;
}
