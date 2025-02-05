package com.trillion.tikitaka.attachment.infrastructure;

import com.trillion.tikitaka.attachment.dto.response.TicketAttachmentResponse;

import java.util.List;

public interface CustomAttachmentRepository {
    List<TicketAttachmentResponse> getTicketAttachments(Long ticketId);
}
