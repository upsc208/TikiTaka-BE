package com.trillion.tikitaka.tickettype.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeCreateRequest {

    @NotBlank(message = "티켓 유형명은 필수입니다.")
    @Length(max = 10, message = "티켓 유형명은 10자 이하여야 합니다.")
    private String typeName;
}
