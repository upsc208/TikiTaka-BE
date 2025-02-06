package com.trillion.tikitaka.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ButtonBlock implements Block {
    private final String type = "button";

    private String text;
    private String style;
    private ButtonAction action;

    public ButtonBlock(String text, String style, ButtonAction action) {
        this.text = text;
        this.style = style;
        this.action = action;
    }
}
