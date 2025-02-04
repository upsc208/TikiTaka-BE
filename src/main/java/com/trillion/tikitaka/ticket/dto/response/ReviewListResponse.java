package com.trillion.tikitaka.ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReviewListResponse {
    private Long reviewId;
    private String reviewerName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @QueryProjection
    public ReviewListResponse(Long reviewId, String reviewerName, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.reviewerName = reviewerName;
        this.createdAt = createdAt;
    }
}
