package com.trillion.tikitaka.user.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 계정 삭제 API (소프트 삭제)
    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteUser(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.softDeleteUser(userId);
        return new ApiResponse<>(null);
    }

    // 현재 로그인한 사용자의 ID 조회 API
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> getCurrentUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ApiResponse<>("현재 로그인한 사용자 ID", userDetails.getUser().getId());
    }
}
