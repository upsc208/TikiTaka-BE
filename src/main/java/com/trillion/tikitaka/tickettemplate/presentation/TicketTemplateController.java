package com.trillion.tikitaka.tickettemplate.presentation;

import com.trillion.tikitaka.tickettemplate.application.TicketTemplateService;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticketTemplates")
@RequiredArgsConstructor
public class TicketTemplateController {

    private final TicketTemplateService templateService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<TicketTemplateCreateResponse> create(
            @Valid @RequestBody TicketTemplateRequest request
    ) {
        Long id = templateService.createTicketTemplate(request);
        TicketTemplateCreateResponse resp = new TicketTemplateCreateResponse("요청 성공", id);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketTemplateRequest request
    ) {
        templateService.updateTicketTemplate(id, request);
        return ResponseEntity.ok("Ticket Template updated successfully");
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTicketTemplate(id);
        return ResponseEntity.ok("Ticket Template deleted successfully");
    }
}
