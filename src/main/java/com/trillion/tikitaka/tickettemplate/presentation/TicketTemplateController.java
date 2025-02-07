package com.trillion.tikitaka.tickettemplate.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateIdResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateListResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticket/templates")
@RequiredArgsConstructor
public class TicketTemplateController {

    private final TicketTemplateService templateService;

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @PostMapping
    public ApiResponse<TicketTemplateIdResponse> create(@Valid @RequestBody TicketTemplateRequest request,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long templateId = templateService.createTicketTemplate(request, userDetails);
        return ApiResponse.success(new TicketTemplateIdResponse(templateId));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @PatchMapping("/{templateId}")
    public ApiResponse<TicketTemplateIdResponse> update(
            @PathVariable Long templateId,
            @Valid @RequestBody TicketTemplateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        templateService.updateTicketTemplate(templateId, request, userDetails);
        return ApiResponse.success(new TicketTemplateIdResponse(templateId));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @DeleteMapping("/{templateId}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long templateId,
                                            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        templateService.deleteTicketTemplate(templateId, customUserDetails);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @GetMapping("/{templateId}")
    public ApiResponse<TicketTemplateResponse> getOne(@PathVariable Long templateId) {
        TicketTemplateResponse data = templateService.getOneTicketTemplate(templateId);
        return ApiResponse.success(data);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @GetMapping
    public ApiResponse<List<TicketTemplateListResponse>> getMyTemplates(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TicketTemplateListResponse> list = templateService.getMyTemplates(userDetails);
        return ApiResponse.success(list);
    }
}
