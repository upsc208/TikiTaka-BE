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
import com.trillion.tikitaka.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @PostMapping
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request,@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long requesterId = userDetails.getId();
        ticketService.createTicket(request,requesterId);
        return new ApiResponse<>("티켓이 생성되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @PatchMapping("/{ticketId}")
    public ApiResponse<Void> editTicket(@PathVariable Long ticketId, @RequestBody @Valid EditTicketRequest request){
        ticketService.editTicket(request,ticketId);
        return new ApiResponse<>("티켓이 수정되었습니다.",null);
    }
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/{status}")
    public ApiResponse<Void> editTicketStatus(@PathVariable Long ticketId, @PathVariable Ticket.Status status, @AuthenticationPrincipal CustomUserDetails userDetails){
        Role role = userDetails.getUser().getRole();
        ticketService.editStatus(ticketId,role,status);
        return new ApiResponse<>("티켓 상태가 수정되었습니다.",null);
    }
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/setting/{ticketId}")
    public ApiResponse<Void> editTicketSetting(@PathVariable Long ticketId, @RequestBody EditSettingRequest editSettingRequest, @AuthenticationPrincipal CustomUserDetails userDetails){
        Role role = userDetails.getUser().getRole();
        ticketService.editSetting(ticketId,role,editSettingRequest);
        return new ApiResponse<>("티켓설정이 수정되었습니다.",null);
    }


    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @DeleteMapping("/{ticketId}")
    public ApiResponse<Void> deleteTicket(@PathVariable Long ticketId){
        ticketService.deleteTicket(ticketId);
        return new ApiResponse<>("티켓이 삭제되었습니다.",null);
    }




}
