package com.trillion.tikitaka.ticket.presentation;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import com.trillion.tikitaka.user.domain.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @PostMapping("/create")
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request) {
        ticketService.createTicket(request);
        return new ApiResponse<>("티켓이 생성되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @PatchMapping("/edit/{ticket_id}")
    public ApiResponse<Void> editTicket(@PathVariable Long ticket_id, @RequestBody @Valid EditTicketRequest request, @AuthenticationPrincipal CustomUserDetails userDetails){
        String role = userDetails.getAuthorities().toString();
        ticketService.editTicket(request,ticket_id);
        return new ApiResponse<>("티켓이 수정되었습니다.",null);
    }
    @PreAuthorize("hasAnyAuthority('MANAGER', 'USER')")
    @PatchMapping("/edit/{ticket_id}/{status}")
    public ApiResponse<Void> editTicketStatus(@PathVariable Long ticket_id, @PathVariable Ticket.Status status, @AuthenticationPrincipal CustomUserDetails userDetails){
        Role role = userDetails.getUser().getRole();
        ticketService.editStatus(ticket_id,role,status);
        return new ApiResponse<>("티켓 상태가 수정되었습니다.",null);
    }
    @PreAuthorize("hasAnyAuthority('MANAGER', 'USER')")
    @PatchMapping("/edit/{ticket_id}/setting")
    public ApiResponse<Void> editTicketSetting(@PathVariable Long ticket_id, @RequestBody EditSettingRequest editSettingRequest, @AuthenticationPrincipal CustomUserDetails userDetails){
        Role role = userDetails.getUser().getRole();
        ticketService.editSetting(ticket_id,role,editSettingRequest);
        return new ApiResponse<>("티켓세팅이 수정되었습니다.",null);
    }


    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @DeleteMapping("/delete/{ticket_id}")
    public ApiResponse<Void> deleteTicket(@PathVariable Long ticket_id){
        ticketService.deleteTicket(ticket_id);
        return new ApiResponse<>("티켓이 삭제되었습니다.",null);
    }




}
