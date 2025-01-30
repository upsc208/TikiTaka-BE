package com.trillion.tikitaka.ticket.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        ticketService.createTicket(request, userDetails.getId());
        return new ApiResponse<>("티켓이 생성되었습니다",null);
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
//    @PatchMapping("/{ticketId}")
//    public ApiResponse<Void> editTicket(@PathVariable("ticketId") Long ticketId,
//                                        @RequestBody @Valid EditTicketRequest request){
//        ticketService.editTicket(request, ticketId);
//        return new ApiResponse<>("티켓이 수정되었습니다.",null);
//    }
//
//    @PreAuthorize("hasAnyAuthority('MANAGER', 'USER')")
//    @PatchMapping("/{ticketId}/{status}")
//    public ApiResponse<Void> editTicketStatus(@PathVariable("ticketId") Long ticketId,
//                                              @PathVariable Ticket.Status status,
//                                              @AuthenticationPrincipal CustomUserDetails userDetails){
//        Role role = userDetails.getUser().getRole();
//        ticketService.editStatus(ticketId, role, status);
//        return new ApiResponse<>("티켓 상태가 수정되었습니다.",null);
//    }
//
//    @PreAuthorize("hasAnyAuthority('MANAGER', 'USER')")
//    @PatchMapping("/{ticketId}/setting")
//    public ApiResponse<Void> editTicketSetting(@PathVariable("ticketId") Long ticketId,
//                                               @RequestBody EditSettingRequest editSettingRequest,
//                                               @AuthenticationPrincipal CustomUserDetails userDetails){
//        Role role = userDetails.getUser().getRole();
//        ticketService.editSetting(ticketId, role, editSettingRequest);
//        return new ApiResponse<>("티켓세팅이 수정되었습니다.",null);
//    }
//
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
//    @DeleteMapping("/{ticketId}")
//    public ApiResponse<Void> deleteTicket(@PathVariable("ticketId") Long ticketId){
//        ticketService.deleteTicket(ticketId);
//        return new ApiResponse<>("티켓이 삭제되었습니다.",null);
//    }
}
