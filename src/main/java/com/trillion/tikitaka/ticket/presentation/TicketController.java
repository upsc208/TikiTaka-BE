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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/create")
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request) {
        ticketService.createTicket(request);
        return new ApiResponse<>("티켓이 생성되었습니다",null);
    }
    @GetMapping("/lists")
    public ResponseEntity<Page<Ticket>> getTicketLists(){
        Page<Ticket> tickets = ticketService.getAllTicket(Pageable.unpaged());
        return ResponseEntity.ok(tickets);
    }
//    @GetMapping("/mylists") //TODO:티켓조회 - 삭제처리된건 불러오면안됨,권한(role)에따라 다양한필터링 필요 권한에 따라 볼수있는 티켓이 다름
//    public ResponseEntity<Page<Ticket>> getTicketListsByRole(@RequestHeader("Authorization") String authorizationHeader) {
//        // 토큰 추출
//        String token = ticketService.extractAccessToken(authorizationHeader);
//
//        Page<Ticket> tickets = ticketService.getTicketsByRole(token,Pageable.unpaged());
//        return ResponseEntity.ok(tickets);
//    }

    @DeleteMapping("/{ticket_id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable("ticket_id") Long ticket_id){
        ticketService.DeleteTicket(ticket_id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{ticket_id}")//TODO: 티켓수정 -> 사용자는 담당자,우선순위변경 불가능
    public ResponseEntity<Void> editTicket(@PathVariable("ticket_id") Long ticket_id,@RequestBody @Valid EditTicketRequest request){
        ticketService.editTicket(request,ticket_id);
        return ResponseEntity.ok().build();
    }



}
