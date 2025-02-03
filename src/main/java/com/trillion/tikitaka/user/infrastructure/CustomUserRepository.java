package com.trillion.tikitaka.user.infrastructure;

import com.trillion.tikitaka.user.dto.response.UserListResponse;

public interface CustomUserRepository {
    UserListResponse findAllUser();
    Long countAdmin();
    Long countManager();
    Long countUser();
}
