package com.trillion.tikitaka.ticketcomment.infrastructure;

import com.trillion.tikitaka.ticketcomment.dto.response.TicketCommentResponse;

import java.util.List;

public interface CustomTicketCommentRepository {
    List<TicketCommentResponse> getTicketComments(Long ticketId);
}
