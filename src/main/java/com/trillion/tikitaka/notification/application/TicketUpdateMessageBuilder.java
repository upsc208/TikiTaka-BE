package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.*;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import com.trillion.tikitaka.notification.event.TicketUpdateEvent;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.user.domain.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TicketUpdateMessageBuilder implements KakaoWorkMessageBuilder<TicketUpdateEvent> {

    @Override
    public List<Block> buildMessage(TicketUpdateEvent event) {
        List<Block> blocks = new ArrayList<>();
        Ticket ticket = event.getTicket();

        // 1. Header Block
        blocks.add(new HeaderBlock("티켓 수정 알림", "yellow"));

        // 2. Text Block (inlines 리스트로 변경)
        String textValue = String.format("[%s] %s", ticket.getId(), ticket.getTitle());
        List<Inline> inlineTexts = List.of(new Inline("styled", textValue, true, "default"));
        blocks.add(new TextBlock(textValue, inlineTexts));

        // 3. Description Block for "변경자"
        String modifierText = event.getModifier();
        List<Inline> inlineType = List.of(new Inline("styled", modifierText, true));
        blocks.add(new DescriptionBlock(new Content(modifierText, inlineType), "변경자", true));

        // 4. Description Block for "변경 필드"
        String modificationText = event.getModification();
        List<Inline> inlineCategory = List.of(new Inline("styled", modificationText, true));
        blocks.add(new DescriptionBlock(new Content(modificationText, inlineCategory), "변경유형", true));

        if (event.getModifierRole() == Role.USER) {
            // 5. Description Block for "변경일시"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String modifiedAtText = event.getModifiedAt().format(formatter);
            List<Inline> inlineManager = List.of(new Inline("styled", modifiedAtText, true));
            blocks.add(new DescriptionBlock(new Content(modifiedAtText, inlineManager), "변경일시", true));
        } else {
            // 5. Description Block for "담당자"
            String managerText = ticket.getManager().getUsername();
            List<Inline> inlineManager = List.of(new Inline("styled", managerText, true));
            blocks.add(new DescriptionBlock(new Content(managerText, inlineManager), "담당자", true));
        }

        return blocks;
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event instanceof TicketUpdateEvent;
    }

    @Override
    public String buildPreviewText(TicketUpdateEvent event) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("%s %d %s has updated %s", date, event.getTicket().getId(), event.getModifier(), event.getModification());
    }
}
