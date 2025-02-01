package com.trillion.tikitaka.tickettemplate.dto.response;

import lombok.Getter;

/**
 * 생성 후 반환 예시입니다~ { "message": "...", "id": 1 }
 */
@Getter
public class TicketTemplateCreateResponse {
    private final String message;
    private final Long id;

    public TicketTemplateCreateResponse(String message, Long id) {
        this.message = message;
        this.id = id;
    }
}
