package com.trillion.tikitaka.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
            message = "비밀번호는 8자리 이상, 알파벳, 숫자, 특수문자를 포함해야 합니다."
    )
    private String newPassword;
}
