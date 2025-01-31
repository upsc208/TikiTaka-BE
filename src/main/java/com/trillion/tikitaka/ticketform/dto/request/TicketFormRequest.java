package com.trillion.tikitaka.ticketform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketFormRequest {

    @NotNull(message = "티켓 폼을 입력해주세요.")
    @Length(max = 5000, message = "티켓 폼은 최대 5000자까지 입력 가능합니다.")
    private String description;
}
