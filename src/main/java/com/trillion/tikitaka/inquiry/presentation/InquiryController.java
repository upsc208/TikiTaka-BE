package com.trillion.tikitaka.inquiry.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.inquiry.application.InquiryService;
import com.trillion.tikitaka.inquiry.dto.request.InquiryRequest;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    public ApiResponse<Void> createInquiry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid InquiryRequest request) {

        inquiryService.createInquiry(userDetails.getUser().getId(), request);
        return new ApiResponse<>(null);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<InquiryResponse>> getAllInquiries() {
        List<InquiryResponse> responses = inquiryService.getAllInquiries();
        return ApiResponse.success(responses);
    }
}
