package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.Block;
import com.trillion.tikitaka.notification.event.NotificationEvent;

import java.util.List;

public interface KakaoWorkMessageBuilder<E extends NotificationEvent> {
    List<Block> buildMessage(E event);
    boolean supports(NotificationEvent event);
}
