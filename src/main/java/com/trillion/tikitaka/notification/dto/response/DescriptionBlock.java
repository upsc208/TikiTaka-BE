package com.trillion.tikitaka.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionBlock implements Block {
    private final String type = "description";
    private Content content;
    private String term;;
    private boolean accent;
}
