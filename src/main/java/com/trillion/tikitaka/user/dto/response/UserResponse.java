package com.trillion.tikitaka.user.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.user.domain.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private Role role;

    @QueryProjection
    public UserResponse(Long userId, String username, String email, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
