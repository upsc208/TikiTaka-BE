package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.event.NotificationEvent;
import com.trillion.tikitaka.notification.exception.InvalidNotificationTypeExceptionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoWorkMessageBuilderDispatcher {

    private final List<KakaoWorkMessageBuilder<? extends NotificationEvent>> builders;

    public KakaoWorkMessageBuilder<? extends NotificationEvent> getBuilder(NotificationEvent event) {
        return builders.stream()
                .filter(builder -> builder.supports(event))
                .findFirst()
                .orElseThrow(InvalidNotificationTypeExceptionException::new);
    }
}
