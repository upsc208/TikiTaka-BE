package com.trillion.tikitaka.inquiry.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.inquiry.application.InquiryService;
import com.trillion.tikitaka.inquiry.dto.request.InquiryRequest;
import com.trillion.tikitaka.inquiry.dto.response.InquiryResponse;
import com.trillion.tikitaka.inquiry.dto.request.AnswerRequest;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Page<InquiryResponse>> getAllInquiries(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size);
        Page<InquiryResponse> responses = inquiryService.getAllInquiries(pageable, userDetails);
        return ApiResponse.success(responses);
    }

    @PatchMapping("/{inquiryId}/answer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> answerInquiry(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestBody @Valid AnswerRequest request) {

        inquiryService.answerInquiry(inquiryId, request.getAnswer());
        return new ApiResponse<>(null);
    }


}
