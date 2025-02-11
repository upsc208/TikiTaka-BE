package com.trillion.tikitaka.ticket.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.subtask.application.SubtaskService;
import com.trillion.tikitaka.ticket.application.ReviewService;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditCategory;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SubtaskService subtaskService;
    private final ReviewService reviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<TicketIdResponse> createTicket(@RequestPart("request") @Valid CreateTicketRequest request,
                                                      @RequestPart(value = "files", required = false) List<@Valid MultipartFile> files,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long ticketId = ticketService.createTicket(request, files, userDetails);
        TicketIdResponse response = new TicketIdResponse(ticketId);
        return new ApiResponse<>("티켓이 생성되었습니다", response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/{ticketId}/approve")
    public ApiResponse<Void> approveTicket(@PathVariable Long ticketId,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.approveTicket(ticketId,userDetails);
        return new ApiResponse<>("티켓이 승인되었습니다.",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/{ticketId}/reject")
    public ApiResponse<Void> rejectTicket(@PathVariable Long ticketId,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        ticketService.rejectTicket(ticketId, userDetails);
        return new ApiResponse<>("티켓이 거절되었습니다.",null);
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
            @RequestParam(value = "date", required = false) String dateOption,  // "today", "week", "month"
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketListResponse> ticketList = ticketService.getTicketList(
                pageable, status, firstCategoryId, secondCategoryId, ticketTypeId, managerId, requesterId, dateOption, sort, userDetails
        );
        return new ApiResponse<>(ticketList);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PatchMapping("/{ticketId}")
    public ApiResponse<Long> editTicket(@PathVariable Long ticketId, @RequestBody @Valid EditTicketRequest request,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editTicket(request,ticketId,userDetails);
        return new ApiResponse<>("티켓이 수정되었습니다.",ticketId);
    }


    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/status")
    public ApiResponse<Void> editTicketStatus(@PathVariable Long ticketId,
                                              @RequestBody EditSettingRequest editSettingRequest,@AuthenticationPrincipal CustomUserDetails userDetails) {
        ticketService.editStatus(ticketId,editSettingRequest.getStatus(),userDetails);
        return new ApiResponse<>("티켓 상태가 수정되었습니다.",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/manager")
    public ApiResponse<Void> editManager(@PathVariable Long ticketId,@RequestBody EditSettingRequest editSettingRequest,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editManager(ticketId,editSettingRequest.getManagerId(),userDetails);
        return new ApiResponse<>("담당자 수정",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/deadline")
    public ApiResponse<Void> editDeadline(@PathVariable Long ticketId, @RequestBody EditSettingRequest editSettingRequest,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editDeadlineForManager(ticketId,editSettingRequest,userDetails);
        return new ApiResponse<>("마감기한 수정",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/priority")
    public ApiResponse<Void> editPriority(@PathVariable Long ticketId, @RequestBody EditSettingRequest editSettingRequest,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editPriority(ticketId,editSettingRequest.getPriority(),userDetails);
        return new ApiResponse<>("우선순위 수정",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/category")
    public ApiResponse<Void> editCategory(@PathVariable Long ticketId, @RequestBody EditCategory editCategory,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editCategoryForManager(editCategory, ticketId,userDetails);
        return new ApiResponse<>("카테고리 수정",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/type")
    public ApiResponse<Void> editType(@PathVariable Long ticketId, @RequestBody EditTicketRequest editSettingRequest,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editTypeForManager(ticketId,editSettingRequest.getTicketTypeId(),userDetails);
        return new ApiResponse<>("티켓 유형 수정",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{ticketId}/urgent")
    public ApiResponse<Void> editUrgent(@PathVariable Long ticketId,@RequestBody EditTicketRequest request,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.editUrgent(ticketId,request,userDetails);
        return new ApiResponse<>("티켓 긴급상태 수정",null);
    }



    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @DeleteMapping("/{ticketId}")
    public ApiResponse<Void> deleteTicket(@PathVariable Long ticketId,@AuthenticationPrincipal CustomUserDetails userDetails){
        subtaskService.deleteAllSubtask(ticketId);
        ticketService.deleteTicket(ticketId,userDetails);
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

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/list/pending")
    public ApiResponse<PendingTicketResponse> getPendingTickets(@RequestParam("managerId") Long managerId) {
        PendingTicketResponse response = ticketService.getPendingTickets(managerId);
        return ApiResponse.success(response);
    }
}
