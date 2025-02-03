package com.trillion.tikitaka.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trillion.tikitaka.notification.dto.response.Block;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class KakaoWorkMessageRequest {

    @JsonProperty("conversation_id")
    private String conversationId;

    private String text;

    private List<Block> blocks;
}
