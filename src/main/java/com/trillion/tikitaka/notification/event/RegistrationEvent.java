package com.trillion.tikitaka.notification.event;

import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RegistrationEvent extends ApplicationEvent implements NotificationEvent {
    private final String username;
    private final String email;
    private final String message;
    private final RegistrationStatus status;
    private final NotificationType type;

    public RegistrationEvent(Object source, String username, String email, String message,
                             RegistrationStatus status, NotificationType type) {
        super(source);
        this.username = username;
        this.email = email;
        this.message = message;
        this.status = status;
        this.type = type;
    }
}
