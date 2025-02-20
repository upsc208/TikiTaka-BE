package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.notification.dto.response.*;
import com.trillion.tikitaka.notification.event.NotificationEvent;
import com.trillion.tikitaka.notification.event.RegistrationEvent;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistrationMessageBuilder implements KakaoWorkMessageBuilder<RegistrationEvent> {

    @Override
    public List<Block> buildMessage(RegistrationEvent event) {
        HeaderBlock header = new HeaderBlock("TikiTaka 계정 등록", "white");
        TextBlock textBlock;
        DescriptionBlock descriptionBlock;

        if (event.getRegistrationStatus() == RegistrationStatus.APPROVED) {
            textBlock = new TextBlock("귀하의 TikiTaka 계정 등록 신청이 승인되었습니다. 임시 발급된 계정 비밀번호는 다음과 같습니다.");
            Inline inline = new Inline("styled", event.getMessage(), true);
            Content content = new Content(event.getMessage(), List.of(inline));
            descriptionBlock = new DescriptionBlock(content, "비밀번호", true);
        } else {
            textBlock = new TextBlock("귀하의 TikiTaka 계정 등록 신청이 거절되었습니다. 거절 사유는 다음과 같습니다.");
            Inline inline = new Inline("styled", event.getMessage(), true);
            Content content = new Content(event.getMessage(), List.of(inline));
            descriptionBlock = new DescriptionBlock(content, "사유", true);
        }

        return List.of(header, textBlock, descriptionBlock);
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event instanceof RegistrationEvent;
    }

    @Override
    public String buildPreviewText(RegistrationEvent event) {
        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        if (event.getRegistrationStatus() == RegistrationStatus.APPROVED) {
            return String.format("%s 계정 등록 신청 approved %s", date, event.getRole().toString());
        } else {
            return String.format("%s 계정 등록 신청 rejected", date);
        }
    }
}
