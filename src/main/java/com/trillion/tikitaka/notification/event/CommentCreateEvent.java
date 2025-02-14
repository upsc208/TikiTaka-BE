package com.trillion.tikitaka.notification.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trillion.tikitaka.notification.domain.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class CommentCreateEvent extends ApplicationEvent implements NotificationEvent {

    private final String email;
    private final Long ticketId;
    private final String ticketTitle;
    private final String firstCategoryName;
    private final String secondCategoryName;
    private final String ticketTypeName;
    private final String author;
    private final String linkUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final NotificationType type;

    public CommentCreateEvent(Object source, String email, Long ticketId, String ticketTitle, String firstCategoryName,
                              String secondCategoryName, String ticketTypeName, String author, String linkUrl) {
        super(source);
        this.email = email;
        this.ticketId = ticketId;
        this.ticketTitle = ticketTitle;
        this.firstCategoryName = firstCategoryName;
        this.secondCategoryName = secondCategoryName;
        this.ticketTypeName = ticketTypeName;
        this.author = author;
        this.linkUrl = linkUrl;
        this.type = NotificationType.COMMENT_CREATE;
    }
}
