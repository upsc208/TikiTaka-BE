package com.trillion.tikitaka.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private final String type = "text";
    private String text;
    private List<Inline> inlines;
}
