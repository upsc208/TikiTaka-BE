package com.trillion.tikitaka.tickettemplate.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateListResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticketTemplates")
@RequiredArgsConstructor
public class TicketTemplateController {

    private final TicketTemplateService templateService;

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TicketTemplateRequest request) {
        Long templateId = templateService.createTicketTemplate(request);
        return ApiResponse.success(templateId);
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

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @GetMapping("/{id}")
    public ApiResponse<TicketTemplateResponse> getOne(@PathVariable Long id) {
        TicketTemplateResponse data = templateService.getOneTicketTemplate(id);
        return ApiResponse.success(data);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @GetMapping
    public ApiResponse<List<TicketTemplateListResponse>> getAll() {
        List<TicketTemplateListResponse> list = templateService.getAllTicketTemplates();
        return ApiResponse.success(list);
    }
}
