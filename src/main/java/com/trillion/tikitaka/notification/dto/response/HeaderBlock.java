package com.trillion.tikitaka.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeaderBlock implements Block {
    private final String type = "header";
    private String text;
    private String style;
}
