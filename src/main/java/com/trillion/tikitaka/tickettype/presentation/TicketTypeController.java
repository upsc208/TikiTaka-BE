package com.trillion.tikitaka.tickettype.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.tickettype.application.TicketTypeService;
import com.trillion.tikitaka.tickettype.dto.request.TicketTypeCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
