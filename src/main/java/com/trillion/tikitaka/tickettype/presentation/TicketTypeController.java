package com.trillion.tikitaka.tickettype.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettype.application.TicketTypeService;
import com.trillion.tikitaka.tickettype.dto.request.TicketTypeRequest;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> createTicketType(@RequestBody @Valid TicketTypeRequest request) {
        ticketTypeService.createTicketType(request.getTypeName());
        return new ApiResponse<>(null);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<List<TicketTypeListResponse>> getTicketTypes() {
        List<TicketTypeListResponse> ticketTypes = ticketTypeService.getTicketTypes();
        return new ApiResponse<>(ticketTypes);
    }

    @PatchMapping("/{typeId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> updateTicketType(@PathVariable("typeId") Long typeId,
                                              @RequestBody @Valid TicketTypeRequest request) {
        ticketTypeService.updateTicketType(typeId, request.getTypeName());
        return new ApiResponse<>(null);
    }

    @DeleteMapping("/{typeId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteTicketType(@PathVariable("typeId") Long typeId) {
        ticketTypeService.deleteTicketType(typeId);
        return new ApiResponse<>(null);
    }
}
