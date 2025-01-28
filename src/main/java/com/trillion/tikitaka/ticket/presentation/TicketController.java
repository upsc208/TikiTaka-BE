package com.trillion.tikitaka.ticket.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @PostMapping("/create")
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request) {
        ticketService.createTicket(request);
        return new ApiResponse<>("티켓이 생성되었습니다",null);
    }




}
