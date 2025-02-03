package com.trillion.tikitaka.user.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/count")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<RegistrationAndUserCountResponse> getRegistrationAndUserCount() {
        RegistrationAndUserCountResponse response = userService.getRegistrationAndUserCount();
        return ApiResponse.success(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<UserListResponse> findAllUsers() {
        UserListResponse response = userService.findAllUsers();
        return ApiResponse.success(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER','USER')")
    public ApiResponse<UserResponse> getUserResponse(@PathVariable("userId") Long userId) {
        UserResponse response = userService.getUserResponse(userId);
        return ApiResponse.success(response);
    }
}
