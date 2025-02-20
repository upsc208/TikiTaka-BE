package com.trillion.tikitaka.notification.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.TicketSnapShot;
import com.trillion.tikitaka.user.domain.Role;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class TicketUpdateEvent extends ApplicationEvent implements NotificationEvent {
    private final String email;
    private final TicketSnapShot ticketSnapshot;
    private final String modifier;
    private final String modification;
    private final Role modifierRole;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime modifiedAt = LocalDateTime.now();
    private final NotificationType type;

    public TicketUpdateEvent(Object source, String email, Ticket ticket, String modifier, String modification, Role modifierRole) {
        super(source);
        this.email = email;
        this.ticketSnapshot = new TicketSnapShot(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getFirstCategory() != null ? ticket.getFirstCategory().getName() : null,
                ticket.getSecondCategory() != null ? ticket.getSecondCategory().getName() : null,
                ticket.getTicketType() != null ? ticket.getTicketType().getName() : "",
                ticket.getManager() != null ? ticket.getManager().getUsername() : null
        );
        this.modifier = modifier;
        this.modification = modification;
        this.modifierRole = modifierRole;
        this.type = NotificationType.TICKET_UPDATE;
    }
}
