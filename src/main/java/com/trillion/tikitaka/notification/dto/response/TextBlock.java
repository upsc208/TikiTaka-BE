package com.trillion.tikitaka.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextBlock implements Block {
    private final String type = "text";
    private String text;
    private List<Inline> inlines;

    public TextBlock(String text) {
        this.text = text;
    }
}
