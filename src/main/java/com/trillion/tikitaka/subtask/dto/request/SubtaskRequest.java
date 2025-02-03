package com.trillion.tikitaka.subtask.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubtaskRequest {
    private Long ticketId;
    private String description;

}
