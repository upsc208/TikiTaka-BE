package com.trillion.tikitaka.ticket.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.subtask.application.SubtaskService;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditCategory;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SubtaskService subtaskService;


    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @PostMapping
    public ApiResponse<Void> createTicket(@RequestBody @Valid CreateTicketRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long requesterId = userDetails.getId();
        ticketService.createTicket(request, requesterId);
        return new ApiResponse<>("티켓이 생성되었습니다", null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/approve/{ticketId}")
    public ApiResponse<Void> approveTicket(@PathVariable Long ticketId,@AuthenticationPrincipal CustomUserDetails userDetails){
        ticketService.approveTicket(ticketId,userDetails);
        return new ApiResponse<>("티켓이 승인되었습니다.",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/reject/{ticketId}")
    public ApiResponse<Void> rejectTicket(@PathVariable Long ticketId){
        ticketService.rejectTicket(ticketId);
        return new ApiResponse<>("티켓이 거절되었습니다.",null);
    }


    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/count")
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/list")
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PatchMapping("/{ticketId}")
    public ApiResponse<Void> editTicket(@PathVariable Long ticketId, @RequestBody @Valid EditTicketRequest request){
        ticketService.editTicket(request,ticketId);
        return new ApiResponse<>("티켓이 수정되었습니다.",null);
    }

    /*@PreAuthorize("hasAnyAuthority('ADMIN','USER')") //사용자 전용 일괄수정,다시고민 해볼것
    @PatchMapping("/{ticketId}")
    public ApiResponse<Void> editTicket(@PathVariable Long ticketId, @RequestBody @Valid EditTicketRequest request){
        ticketService.editTitle(request,ticketId);
        ticketService.editCategory(request,ticketId);
        ticketService.editDescription(request,ticketId);
        ticketService.editType(request,ticketId);
        ticketService.editUrgent(request,ticketId);
        ticketService.editDeadline(ticketId,request);
        return new ApiResponse<>("티켓 세부 내용이 수정되었습니다.",null);
    }*/

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/status/{ticketId}")
    public ApiResponse<Void> editTicketStatus(@PathVariable Long ticketId,
                                              @RequestBody Ticket.Status status) {
        ticketService.editStatus(ticketId,status);
        return new ApiResponse<>("티켓 상태가 수정되었습니다.",null);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/manager/{ticketId}")
    public ApiResponse<Void> editManager(@PathVariable Long ticketId,@RequestBody Long managerId){
        ticketService.editManager(ticketId,managerId);
        return new ApiResponse<>("담당자 수정",null);
        //return new ApiResponse.success(); //추후 수정
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/deadline/{ticketId}")
    public ApiResponse<Void> editDeadline(@PathVariable Long ticketId, @RequestBody EditSettingRequest editSettingRequest){
        ticketService.editDeadlineForManager(ticketId,editSettingRequest);
        return new ApiResponse<>("마감기한 수정",null);
        //return new ApiResponse.success(); //추후 수정
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/priority/{ticketId}")
    public ApiResponse<Void> editPriority(@PathVariable Long ticketId, @RequestBody Ticket.Priority priority){
        ticketService.editPriorty(ticketId,priority);
        return new ApiResponse<>("우선순위 수정",null);
        //return new ApiResponse.success(); //추후 수정
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/category/{ticketId}")
    public ApiResponse<Void> editCategory(@PathVariable Long ticketId, @RequestBody EditCategory editCategory){
        ticketService.editCategoryForManager(editCategory.getFirstCategoryId(), editCategory.getSecondCategoryId(), ticketId);
        return new ApiResponse<>("카테고리 수정",null);
        //return new ApiResponse.success(); //추후 수정
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/type/{ticketId}")
    public ApiResponse<Void> editType(@PathVariable Long ticketId, @RequestBody Long typeId){
        ticketService.editTypeForManager(ticketId,typeId);
        return new ApiResponse<>("티켓 유형 수정",null);
        //return new ApiResponse.success(); //추후 수정
    }


    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @DeleteMapping("/{ticketId}")
    public ApiResponse<Void> deleteTicket(@PathVariable Long ticketId){
        subtaskService.deleteAllSubtask(ticketId);
        ticketService.deleteTicket(ticketId);
        return new ApiResponse<>("티켓이 삭제되었습니다.", null);
    }
}
