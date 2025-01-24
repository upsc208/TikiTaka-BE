package com.trillion.tikitaka.tickettype.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettype.application.TicketTypeService;
import com.trillion.tikitaka.tickettype.dto.request.TicketTypeCreateRequest;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    public ApiResponse<Void> createTicketType(@RequestBody @Valid TicketTypeCreateRequest request) {
        ticketTypeService.createTicketType(request.getTypeName());
        return new ApiResponse<>(null);
    }

    @GetMapping("/list")
    public ApiResponse<List<TicketTypeListResponse>> getTicketTypes(
            @RequestParam(value = "active", required = false) Boolean active,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TicketTypeListResponse> ticketTypes = ticketTypeService.getTicketTypes(active, userDetails);
        return new ApiResponse<>(ticketTypes);
    }
}
