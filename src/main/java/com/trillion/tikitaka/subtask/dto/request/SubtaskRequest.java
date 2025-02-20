package com.trillion.tikitaka.subtask.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubtaskRequest {

    private Long ticketId;

    @NotNull(message = "내용을 입력해주세요.")
    @Size(max = 100, message = "내용은 100자를 초과할 수 없습니다.")
    private String description;

    public SubtaskRequest(Long id,String content){
        this.ticketId = id;
        this.description = content;
    }

}
