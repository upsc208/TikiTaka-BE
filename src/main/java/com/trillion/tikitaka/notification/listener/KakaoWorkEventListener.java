package com.trillion.tikitaka.notification.listener;

import com.trillion.tikitaka.notification.application.KakaoWorkMessageBuilder;
import com.trillion.tikitaka.notification.application.KakaoWorkMessageBuilderDispatcher;
import com.trillion.tikitaka.notification.application.KakaoWorkNotificationService;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoWorkEventListener {

    private final KakaoWorkNotificationService notificationService;
    private final KakaoWorkMessageBuilderDispatcher builderDispatcher;

    @Async
    @EventListener
    public void handleKakaoWorkNotificationEvent(NotificationEvent event) {
        String email = event.getEmail();
        KakaoWorkMessageBuilder<NotificationEvent> builder = builderDispatcher.getBuilder(event);
        var blocks = builder.buildMessage(event);

        notificationService.sendKakaoWorkNotification(email, blocks)
                .doOnSuccess(unused -> System.out.println("카카오워크 알림 전송 성공"))
                .doOnError(error -> System.err.println("카카오워크 알림 전송 실패"))
                .subscribe();
    }
}
