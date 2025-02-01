package com.trillion.tikitaka.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoWorkUserRequest {
    private boolean success;
    private User user;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String id;
        private String name;
    }
}