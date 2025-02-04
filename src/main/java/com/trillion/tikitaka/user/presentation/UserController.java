package com.trillion.tikitaka.user.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.dto.PasswordChangeRequest;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER') and #userId == authentication.principal.id")
    public ApiResponse<Void> changePassword(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid PasswordChangeRequest request) {
        userService.changePassword(userId, request);
        return new ApiResponse<>(null);
    }

    // 현재 로그인한 사용자의 ID 조회 API
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> getCurrentUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ApiResponse<>("현재 로그인한 사용자 ID", userDetails.getUser().getId());
    }
}
