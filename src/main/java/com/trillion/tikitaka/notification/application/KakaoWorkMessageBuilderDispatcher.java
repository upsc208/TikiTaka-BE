package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.event.NotificationEvent;
import com.trillion.tikitaka.notification.exception.InvalidNotificationTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoWorkMessageBuilderDispatcher {

    private final List<KakaoWorkMessageBuilder<? extends NotificationEvent>> builders;

    @SuppressWarnings("unchecked")
    public <T extends NotificationEvent> KakaoWorkMessageBuilder<T> getBuilder(T event) {
        return (KakaoWorkMessageBuilder<T>) builders.stream()
                .filter(builder -> builder.supports(event))
                .findFirst()
                .orElseThrow(InvalidNotificationTypeException::new);
    }
}
