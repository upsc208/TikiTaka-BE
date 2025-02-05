package com.trillion.tikitaka.tickettemplate.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse; // 새 DTO
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ticketTemplates")
@RequiredArgsConstructor
public class TicketTemplateController {

    private final TicketTemplateService templateService;

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @PostMapping
    public ApiResponse<Map<String, Object>> create(
            @Valid @RequestBody TicketTemplateRequest request
    ) {
        Long id = templateService.createTicketTemplate(request);
        return ApiResponse.success(Map.of("id", id));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @PatchMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketTemplateRequest request
    ) {
        templateService.updateTicketTemplate(id, request);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTicketTemplate(id);
        return ApiResponse.success(null);
    }

    // [신규] 단일 조회
    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @GetMapping("/{id}")
    public ApiResponse<TicketTemplateResponse> getOne(@PathVariable Long id) {
        TicketTemplateResponse data = templateService.getOneTicketTemplate(id);
        return ApiResponse.success(data);
    }
}
