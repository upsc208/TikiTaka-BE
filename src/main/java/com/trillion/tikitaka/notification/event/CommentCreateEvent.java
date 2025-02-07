package com.trillion.tikitaka.notification.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.ticket.domain.Ticket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class CommentCreateEvent extends ApplicationEvent implements NotificationEvent {
    private final String email;
    private final Ticket ticket;
    private final String author;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final NotificationType type;

    public CommentCreateEvent(Object source, String email, Ticket ticket, String author) {
        super(source);
        this.email = email;
        this.ticket = ticket;
        this.author = author;
        this.type = NotificationType.COMMENT_CREATE;
    }
}
