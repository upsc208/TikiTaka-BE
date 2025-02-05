package com.trillion.tikitaka.inquiry.dto.response;

import com.trillion.tikitaka.inquiry.domain.Inquiry;
import lombok.Getter;

@Getter
public class InquiryResponse {
    private final Long id;
    private final String content;
    private final Long writerId;
    private final String writerUsername;

    public InquiryResponse(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.content = inquiry.getContent();
        this.writerId = inquiry.getWriter().getId();
        this.writerUsername = inquiry.getWriter().getUsername();
    }
}
