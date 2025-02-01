package com.trillion.tikitaka.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeaderBlock implements Block {

    @JsonProperty("type")
    private final String type = "header";

    private String text;
    private String style;
}
