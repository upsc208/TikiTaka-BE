package com.trillion.tikitaka.subtask.dto.response;

import com.trillion.tikitaka.subtask.domain.Subtask;
import lombok.Getter;

@Getter
public class SubtaskResponse {
    private Long subtaskId;
    private Long parentId;
    private String description;
    private boolean isDone;

    public SubtaskResponse(Subtask subtask) {
        this.subtaskId = subtask.getId();
        this.parentId = subtask.getParentTicket().getId();
        this.description = subtask.getDescription();
        this.isDone = subtask.isDone();
    }
}
