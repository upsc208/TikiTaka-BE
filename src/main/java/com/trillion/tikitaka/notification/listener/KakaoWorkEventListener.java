package com.trillion.tikitaka.notification.listener;

import com.trillion.tikitaka.notification.application.KakaoWorkMessageBuilder;
import com.trillion.tikitaka.notification.application.KakaoWorkMessageBuilderDispatcher;
import com.trillion.tikitaka.notification.application.KakaoWorkNotificationService;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.notification.dto.response.Block;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoWorkEventListener {

    private final KakaoWorkNotificationService notificationService;
    private final KakaoWorkMessageBuilderDispatcher builderDispatcher;

    @Async
    @EventListener
    public void handleKakaoWorkNotificationEvent(NotificationEvent event) {
        log.info("[알림 요청 이벤트 수신] 이메일: {}, 알림 유형: {}", event.getEmail(), event.getType());
        String email = event.getEmail();
        NotificationType type = event.getType();

        KakaoWorkMessageBuilder<NotificationEvent> builder = builderDispatcher.getBuilder(event);

        List<Block> blocks = builder.buildMessage(event);
        String previewText = builder.buildPreviewText(event);

        notificationService.sendKakaoWorkNotification(email, previewText, blocks, type)
                .subscribe();
    }
}
