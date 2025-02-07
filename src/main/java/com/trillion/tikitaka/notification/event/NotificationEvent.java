package com.trillion.tikitaka.notification.event;

import com.trillion.tikitaka.notification.domain.NotificationType;

public interface NotificationEvent {
    String getEmail();
    NotificationType getType();
}
