package com.trillion.tikitaka.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoWorkConversationRequest {
    private Conversation conversation;
    private boolean isNew;
    private boolean success;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Conversation {
        private String id;
        private String name;
    }
}