package com.trillion.tikitaka.user.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
}
