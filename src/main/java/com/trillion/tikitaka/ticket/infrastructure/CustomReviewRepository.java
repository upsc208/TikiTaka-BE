package com.trillion.tikitaka.ticket.infrastructure;

import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;

import java.util.List;

public interface CustomReviewRepository {
    List<ReviewListResponse> findAllByTicketId(Long ticketId);
}
