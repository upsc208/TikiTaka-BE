package com.trillion.tikitaka.inquiry.dto.request;

import com.trillion.tikitaka.inquiry.domain.InquiryType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private InquiryType type;
}
