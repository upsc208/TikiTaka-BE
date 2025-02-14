package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.*;
import com.trillion.tikitaka.notification.event.CommentCreateEvent;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommentCreateMessageBuilder implements KakaoWorkMessageBuilder<CommentCreateEvent> {

    @Override
    public List<Block> buildMessage(CommentCreateEvent event) {
        List<Block> blocks = new ArrayList<>();
        Long ticketId = event.getTicketId();
        String ticketTitle = event.getTicketTitle();

        // 1. Header Block
        blocks.add(new HeaderBlock("댓글 작성 알림", "yellow"));

        // 2. Text Block (inlines 리스트로 변경)
        String textValue = String.format("[#%s] %s", ticketId, ticketTitle);
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
        ButtonAction action = new ButtonAction("open_system_browser", "확인하기", event.getLinkUrl());
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

        Long ticketId = event.getTicketId();
        String firstCategoryName = event.getFirstCategoryName();
        String secondCategoryName = event.getSecondCategoryName();
        String ticketTypeName = event.getTicketTypeName();

        if (firstCategoryName == null) {
            return String.format("%s-%s-%d 댓글 작성", date, ticketTypeName, ticketId);
        } else {
            String secondPart = (secondCategoryName != null) ? secondCategoryName : "-";
            return String.format("%s/%s/%s/%s-#%d 댓글 작성",
                    date, firstCategoryName, secondPart, ticketTypeName, ticketId);
        }
    }
}
