package com.trillion.tikitaka.inquiry.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryType {
    QUESTION("질문"),
    REQUEST("요청");

    private final String description;

    public String getKoreanName() {
        return this.description;
    }
}
