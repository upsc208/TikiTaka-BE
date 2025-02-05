package com.trillion.tikitaka.user.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.dto.request.PasswordChangeRequest;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.trillion.tikitaka.user.dto.request.RoleChangeRequest;


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

    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER') and #userId == authentication.principal.id")
    public ApiResponse<Void> changePassword(@PathVariable("userId") Long userId,
                                            @RequestBody @Valid PasswordChangeRequest request) {
        userService.updatePassword(userId, request);
        return new ApiResponse<>(null);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return new ApiResponse<>(null);
    }
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> changeUserRole(@PathVariable("userId") Long userId,
                                            @RequestBody @Valid RoleChangeRequest request) {
        userService.changeUserRole(userId, request.getRole());
        return new ApiResponse<>(null);
    }

}