package com.trillion.tikitaka.registration.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.registration.application.RegistrationService;
import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ApiResponse<Void> createRegistration(@RequestBody @Valid RegistrationRequest registrationRequest) {
        registrationService.createRegistration(registrationRequest);
        return new ApiResponse<>(null);
    }
}
