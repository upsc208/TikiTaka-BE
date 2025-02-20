package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.Block;
import com.trillion.tikitaka.notification.event.NotificationEvent;

import java.util.List;

public interface KakaoWorkMessageBuilder<E extends NotificationEvent> {
    List<Block> buildMessage(E event);
    boolean supports(NotificationEvent event);

    default String buildPreviewText(E event) {
        return "알림이 전송되었습니다.";
    }
}
