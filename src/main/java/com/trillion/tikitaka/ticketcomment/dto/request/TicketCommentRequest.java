package com.trillion.tikitaka.ticketcomment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentRequest {

    @NotBlank(message = "댓글을 작성해주세요")
    @Length(max = 1000, message = "댓글은 1000자 이내로 작성해주세요")
    private String content;
}
