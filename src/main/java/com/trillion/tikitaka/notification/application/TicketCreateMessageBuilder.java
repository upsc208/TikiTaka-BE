package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.*;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import com.trillion.tikitaka.notification.event.TicketCreationEvent;
import com.trillion.tikitaka.ticket.domain.Ticket;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TicketCreateMessageBuilder implements KakaoWorkMessageBuilder<TicketCreationEvent> {

    @Override
    public List<Block> buildMessage(TicketCreationEvent event) {
        List<Block> blocks = new ArrayList<>();
        Ticket ticket = event.getTicket();

        // 1. Header Block
        blocks.add(new HeaderBlock("티켓 생성 알림", "blue"));

        // 2. Text Block (inlines 리스트로 변경)
        String textValue = String.format("[%s] %s", ticket.getId(), ticket.getTitle());
        List<Inline> inlineTexts = List.of(new Inline("styled", textValue, true, "blue")); // 🔹 리스트로 변경
        blocks.add(new TextBlock(textValue, inlineTexts));

        // 3. Description Block for "유형"
        String typeText = (ticket.getTicketType() != null) ? ticket.getTicketType().toString() : "-";
        List<Inline> inlineType = List.of(new Inline("styled", typeText, true));
        blocks.add(new DescriptionBlock(new Content(typeText, inlineType), "유형", true));

        // 4. Description Block for "카테고리"
        String categoryText = getCategoryText(ticket);
        List<Inline> inlineCategory = List.of(new Inline("styled", categoryText, true));
        blocks.add(new DescriptionBlock(new Content(categoryText, inlineCategory), "카테고리", true));

        // 5. Description Block for "담당자"
        String managerText = (ticket.getManager() == null) ? "미배정" : ticket.getManager().getUsername();
        List<Inline> inlineManager = List.of(new Inline("styled", managerText, true));
        blocks.add(new DescriptionBlock(new Content(managerText, inlineManager), "담당자", true));

        // 6. Description Block for "요청자"
        String requesterText = (ticket.getRequester() != null) ? ticket.getRequester().getUsername() : "-";
        List<Inline> inlineRequester = List.of(new Inline("styled", requesterText, true));
        blocks.add(new DescriptionBlock(new Content(requesterText, inlineRequester), "요청자", true));

        // 7. Button Block for "확인하기"
        String url = "https://tikitaka.kr/manager/detail/" + ticket.getId();
        ButtonAction action = new ButtonAction("open_system_browser", "확인하기", url);
        ButtonBlock buttons = new ButtonBlock("확인하기", "default", action);
        blocks.add(buttons);

        return blocks;
    }

    private String getCategoryText(Ticket ticket) {
        if (ticket.getFirstCategory() == null && ticket.getSecondCategory() == null) {
            return "-";
        }
        if (ticket.getFirstCategory() != null && ticket.getSecondCategory() != null) {
            return ticket.getFirstCategory() + "/" + ticket.getSecondCategory();
        }
        return ticket.getFirstCategory() != null ? ticket.getFirstCategory().toString() : ticket.getSecondCategory().toString();
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event instanceof TicketCreationEvent;
    }

    @Override
    public String buildPreviewText(TicketCreationEvent event) {
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        Ticket ticket = event.getTicket();

        String firstCategoryName = (ticket.getFirstCategory() != null)
                ? ticket.getFirstCategory().getName()
                : null;

        String secondCategoryName = (ticket.getSecondCategory() != null)
                ? ticket.getSecondCategory().getName()
                : null;

        String categoryPart = buildCategoryString(firstCategoryName, secondCategoryName);

        String ticketTypeName = (ticket.getTicketType() != null)
                ? ticket.getTicketType().getName()
                : "";

        if (categoryPart.isEmpty()) {
            return String.format("%s-%d-%s created", date, ticket.getId(), ticketTypeName);
        } else {
            return String.format("%s-%d-%s-%s created", date, ticket.getId(), categoryPart, ticketTypeName);
        }
    }

    private String buildCategoryString(String firstCategory, String secondCategory) {
        if (firstCategory == null && secondCategory == null) {
            return "";
        }

        if (firstCategory != null && secondCategory != null) {
            return firstCategory + "/" + secondCategory;
        }

        if (firstCategory != null) {
            return firstCategory;
        }
        return secondCategory;
    }
}
