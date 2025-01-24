package com.trillion.tikitaka.registration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationProcessReasonRequest {

    @Length(max = 500, message = "사유는 500자 이하여야 합니다.")
    private String reason;
}
