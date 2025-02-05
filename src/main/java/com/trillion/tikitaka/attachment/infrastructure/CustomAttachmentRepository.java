package com.trillion.tikitaka.attachment.infrastructure;

import com.trillion.tikitaka.attachment.dto.response.AttachmentResponse;

import java.util.List;

public interface CustomAttachmentRepository {
    List<AttachmentResponse> getTicketAttachments(Long ticketId);
}
