package com.trillion.tikitaka.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Inline {
    private String type;
    private String text;
    private boolean bold;
    private String color;

    public Inline(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public Inline(String type, String text, boolean bold) {
        this.type = type;
        this.text = text;
        this.bold = bold;
    }

    public Inline(String type, String text, String color) {
        this.type = type;
        this.text = text;
        this.color = color;
    }
}
