package com.trillion.tikitaka.registration.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.registration.application.RegistrationService;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.request.RegistrationProcessReasonRequest;
import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/list")
    public ApiResponse<Page<RegistrationListResponse>> getRegistrations(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "status", required = false) RegistrationStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RegistrationListResponse> registrations = registrationService.getRegistrations(status, pageable);

        return new ApiResponse<>(registrations);
    }

    @PostMapping("/{registrationId}")
    public ApiResponse<Void> processRegistration(@PathVariable("registrationId") Long registrationId,
                                                 @RequestParam(value = "status", required = false) RegistrationStatus status,
                                                 @RequestBody @Valid RegistrationProcessReasonRequest request) {
        registrationService.processRegistration(registrationId, status, request);
        return new ApiResponse<>(null);
    }
}
