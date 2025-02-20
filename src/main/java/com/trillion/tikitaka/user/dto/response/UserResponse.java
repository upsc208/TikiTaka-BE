package com.trillion.tikitaka.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.user.domain.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String profileImageUrl;

    @QueryProjection
    public UserResponse(Long userId, String username, String email, Role role, String profileImageUrl) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.profileImageUrl = (profileImageUrl != null) ? profileImageUrl : "";
    }
}
