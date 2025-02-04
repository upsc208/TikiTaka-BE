package com.trillion.tikitaka.user.infrastructure;

import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;

public interface CustomUserRepository {
    UserListResponse findAllUser();
    UserResponse getUserResponse(Long userId);
    Long countAdmin();
    Long countManager();
    Long countUser();
}
