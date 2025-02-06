package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.*;
import com.trillion.tikitaka.notification.event.CommentCreateEvent;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import com.trillion.tikitaka.ticket.domain.Ticket;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.trillion.tikitaka.notification.dto.response.ButtonBlock.END_POINT;

@Component
public class CommentCreateMessageBuilder implements KakaoWorkMessageBuilder<CommentCreateEvent> {

    @Override
    public List<Block> buildMessage(CommentCreateEvent event) {
        List<Block> blocks = new ArrayList<>();
        Ticket ticket = event.getTicket();

        // 1. Header Block
        blocks.add(new HeaderBlock("댓글 작성 알림", "yellow"));

        // 2. Text Block (inlines 리스트로 변경)
        String textValue = String.format("[%s] %s", ticket.getId(), ticket.getTitle());
        List<Inline> inlineTexts = List.of(new Inline("styled", textValue, true, "default"));
        blocks.add(new TextBlock(textValue, inlineTexts));

        // 3. Description Block for "작성자"
        String authorText = event.getAuthor();
        List<Inline> inlineType = List.of(new Inline("styled", authorText, true));
        blocks.add(new DescriptionBlock(new Content(authorText, inlineType), "작성자", true));

        // 4. Description Block for "작성일시"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String modifiedAtText = event.getCreatedAt().format(formatter);
        List<Inline> inlineManager = List.of(new Inline("styled", modifiedAtText, true));
        blocks.add(new DescriptionBlock(new Content(modifiedAtText, inlineManager), "작성일시", true));

        // 5. Description Block for "내용"
        String url;
        if (event.getAuthor().equals(ticket.getRequester().getUsername())) {
            url = END_POINT + "/manager/detail/" + ticket.getId();
        } else {
            url = END_POINT + "/user/detail/" + ticket.getId();
        }

        ButtonAction action = new ButtonAction("open_system_browser", "확인하기", url);
        ButtonBlock block = new ButtonBlock("확인하기", "default", action);
        blocks.add(block);

        return blocks;
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event instanceof CommentCreateEvent;
    }

    @Override
    public String buildPreviewText(CommentCreateEvent event) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        String username = event.getAuthor();
        Long ticketId = event.getTicket().getId();

        return String.format("%s %s has commented on %d", date, username, ticketId);

    }
}
