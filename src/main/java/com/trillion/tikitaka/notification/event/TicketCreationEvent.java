package com.trillion.tikitaka.notification.event;

import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.ticket.domain.Ticket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TicketCreationEvent extends ApplicationEvent implements NotificationEvent {
    private final String email;
    private final Ticket ticket;
    private final NotificationType type;

    public TicketCreationEvent(Object source, String email, Ticket ticket, NotificationType type) {
        super(source);
        this.email = email;
        this.ticket = ticket;
        this.type = type;
    }
}
