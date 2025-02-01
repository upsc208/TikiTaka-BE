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
public class DescriptionBlock implements Block {

    @JsonProperty("type")
    private final String type = "description";

    private Content content;
    private String term;;
    private boolean accent;
}
