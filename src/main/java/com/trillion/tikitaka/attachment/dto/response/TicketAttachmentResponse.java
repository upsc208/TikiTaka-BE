package com.trillion.tikitaka.attachment.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketAttachmentResponse {
    private Long attachmentId;
    private String fileName;
    private String filePath;
    private Long fileSize;

    @QueryProjection
    public TicketAttachmentResponse(Long attachmentId, String fileName, String filePath, Long fileSize) {
        this.attachmentId = attachmentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
}
