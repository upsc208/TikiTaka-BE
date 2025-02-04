package com.trillion.tikitaka.ticket.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.subtask.application.SubtaskService;
import com.trillion.tikitaka.ticket.application.ReviewService;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketResponse;
import com.trillion.tikitaka.user.domain.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SubtaskService subtaskService;
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long requesterId = userDetails.getId();
        ticketService.createTicket(request, requesterId);
        return new ApiResponse<>("티켓이 생성되었습니다", null);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<TicketCountByStatusResponse> countTicketsByStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        TicketCountByStatusResponse response = ticketService.countTicketsByStatus(userDetails);
        return new ApiResponse<>(response);
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<TicketResponse> getTicket(@PathVariable("ticketId") Long ticketId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        TicketResponse ticket = ticketService.getTicket(ticketId, userDetails);
        return new ApiResponse<>(ticket);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Page<TicketListResponse>> getTicketList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "status", required = false) Ticket.Status status,
            @RequestParam(value = "firstCategoryId", required = false) Long firstCategoryId,
            @RequestParam(value = "secondCategoryId", required = false) Long secondCategoryId,
            @RequestParam(value = "ticketTypeId", required = false) Long ticketTypeId,
            @RequestParam(value = "managerId", required = false) Long managerId,
            @RequestParam(value = "requesterId", required = false) Long requesterId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketListResponse> ticketList = ticketService.getTicketList(
                pageable, status, firstCategoryId, secondCategoryId, ticketTypeId, managerId, requesterId, userDetails
        );
        return new ApiResponse<>(ticketList);
    }

    @PatchMapping("/{ticketId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Void> editTicket(@PathVariable Long ticketId, @RequestBody @Valid EditTicketRequest request) {
        ticketService.editTicket(request, ticketId);
        return new ApiResponse<>("티켓이 수정되었습니다.", null);
    }

    @PatchMapping("/{ticketId}/{status}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ApiResponse<Void> editTicketStatus(@PathVariable Long ticketId,
                                              @PathVariable Ticket.Status status,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Role role = userDetails.getUser().getRole();
        ticketService.editStatus(ticketId, role, status);
        return new ApiResponse<>("티켓 상태가 수정되었습니다.", null);
    }

    @PatchMapping("/setting/{ticketId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ApiResponse<Void> editTicketSetting(@PathVariable Long ticketId,
                                               @RequestBody EditSettingRequest editSettingRequest,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Role role = userDetails.getUser().getRole();
        ticketService.editSetting(ticketId, role, editSettingRequest);
        return new ApiResponse<>("티켓설정이 수정되었습니다.", null);
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Void> deleteTicket(@PathVariable Long ticketId) {
        subtaskService.deleteAllSubtask(ticketId);
        ticketService.deleteTicket(ticketId);
        return new ApiResponse<>("티켓이 삭제되었습니다.", null);
    }

    @PostMapping("/{ticketId}/reviews")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ApiResponse<Void> doReview(@PathVariable("ticketId") Long ticketId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.doReview(ticketId, userDetails.getId());
        return new ApiResponse<>("티켓 검토를 완료했습니다.", null);
    }

    @GetMapping("/{ticketId}/reviews")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ApiResponse<List<ReviewListResponse>> getReviews(@PathVariable("ticketId") Long ticketId) {
        List<ReviewListResponse> responses = reviewService.getReviews(ticketId);
        return new ApiResponse<>("검토 목록이 조회되었습니다.", responses);
    }
}
