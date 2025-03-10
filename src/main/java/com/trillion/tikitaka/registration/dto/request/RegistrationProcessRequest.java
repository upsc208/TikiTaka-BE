package com.trillion.tikitaka.registration.dto.request;

import com.trillion.tikitaka.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationProcessRequest {

    private Role role;

    @Length(max = 500, message = "사유는 500자 이하여야 합니다.")
    private String reason;
}
