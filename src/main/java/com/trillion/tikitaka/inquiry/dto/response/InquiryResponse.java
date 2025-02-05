package com.trillion.tikitaka.inquiry.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trillion.tikitaka.inquiry.domain.Inquiry;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryResponse {
    private final Long inquiryId;
    private final Long requesterId;
    private final String requesterName;
    private final String type;
    private final String title;
    private final String content;
    private final String answer;
    private final boolean status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime updatedAt;

    public InquiryResponse(Inquiry inquiry) {
        this.inquiryId = inquiry.getId();
        this.requesterId = inquiry.getRequester().getId();
        this.requesterName = inquiry.getRequester().getUsername();
        this.type = inquiry.getType().name();
        this.title = inquiry.getTitle();
        this.content = inquiry.getContent();
        this.answer = inquiry.getAnswer();
        this.status = inquiry.isStatus();
        this.createdAt = inquiry.getCreatedAt(); 
        this.updatedAt = inquiry.getUpdatedAt();
    }
}
