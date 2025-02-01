package com.trillion.tikitaka.tickettemplate.presentation.controller;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettemplate.application.service.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticketTemplates")
@RequiredArgsConstructor
public class TicketTemplateController {

    private final TicketTemplateService templateService;

    // 권한: @PreAuthorize("hasRole('USER')") => 사용자 접근
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createTicketTemplate(@RequestBody TicketTemplateRequest request) {
        // 예외 처리 X
        Long id = templateService.createTicketTemplate(request);
        // 성공 시 ID 반환
        return ResponseEntity.ok(ApiResponse.success(id));
    }
}
