package com.trillion.tikitaka.user.dto.request;

import com.trillion.tikitaka.user.domain.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleChangeRequest {
    @NotNull
    private Role role;
}

