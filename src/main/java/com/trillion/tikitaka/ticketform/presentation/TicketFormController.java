package com.trillion.tikitaka.ticketform.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.ticketform.application.TicketFormService;
import com.trillion.tikitaka.ticketform.dto.request.TicketFormRequest;
import com.trillion.tikitaka.ticketform.dto.response.TicketFormResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets/forms")
@RequiredArgsConstructor
public class TicketFormController {

    private final TicketFormService ticketFormService;

    @PostMapping("/{firstCategoryId}/{secondCategoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> createTicketForm(@PathVariable("firstCategoryId") Long firstCategoryId,
                                              @PathVariable("secondCategoryId") Long secondCategoryId,
                                              @RequestBody @Valid TicketFormRequest request) {
        ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, request.getDescription());
        return new ApiResponse<>(null);
    }

    @GetMapping("/{firstCategoryId}/{secondCategoryId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<TicketFormResponse> getTicketForm(@PathVariable("firstCategoryId") Long firstCategoryId,
                                                         @PathVariable("secondCategoryId") Long secondCategoryId) {
        TicketFormResponse ticketForm = ticketFormService.getTicketForm(firstCategoryId, secondCategoryId);
        return new ApiResponse<>(ticketForm);
    }

    @PatchMapping("/{firstCategoryId}/{secondCategoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> updateTicketForm(@PathVariable("firstCategoryId") Long firstCategoryId,
                                              @PathVariable("secondCategoryId") Long secondCategoryId,
                                              @RequestBody @Valid TicketFormRequest request) {
        ticketFormService.updateTicketForm(firstCategoryId, secondCategoryId, request.getDescription());
        return new ApiResponse<>(null);
    }

    @DeleteMapping("/{firstCategoryId}/{secondCategoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteTicketForm(@PathVariable("firstCategoryId") Long firstCategoryId,
                                              @PathVariable("secondCategoryId") Long secondCategoryId) {
        ticketFormService.deleteTicketForm(firstCategoryId, secondCategoryId);
        return new ApiResponse<>(null);
    }
}
