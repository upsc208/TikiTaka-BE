package com.trillion.tikitaka.user.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.dto.PasswordChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER') and #userId == authentication.principal.id")
    public ApiResponse<Void> changePassword(@PathVariable("userId") Long userId,
                                            @RequestBody @Valid PasswordChangeRequest request) {
        userService.updatePassword(userId, request);
        return new ApiResponse<>(null);
    }

    // 계정 삭제 API (소프트 삭제)
    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return new ApiResponse<>(null);
    }
}