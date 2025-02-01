package com.trillion.tikitaka.subtask.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.subtask.application.SubtaskService;
import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;
    private final SubtaskRepository subtaskRepository;

    @PostMapping
    public ApiResponse<Void> createSubtask(@RequestBody SubtaskRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Subtask subtask = subtaskService.createSubtask(request,userDetails);
        subtaskRepository.save(subtask);
        return new ApiResponse<>("태스크가 생성되었습니다",null);
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<List<SubtaskResponse>> getSubtask(@PathVariable Long ticketId){
        List<SubtaskResponse> subtasks = subtaskService.getSubtasksByTicketId(ticketId);
        return new ApiResponse<>(subtasks);
    }
    @DeleteMapping("/{ticketId}/{taskId}")
    public ApiResponse<Void> deleteSubtask(@PathVariable Long ticketId,@PathVariable Long taskId,@AuthenticationPrincipal CustomUserDetails userDetails){
        subtaskService.deleteSubtask(ticketId,taskId,userDetails);
        return new ApiResponse<>("태스크가 삭제되었습니다",null);
    }
    @PatchMapping("/{ticketId}/{taskId}")
    public ApiResponse<Void> editSubtask(@PathVariable Long ticketId, @PathVariable Long taskId, @RequestBody SubtaskRequest request, @AuthenticationPrincipal CustomUserDetails userDetails){
        subtaskService.editSubtask(ticketId,taskId,request,userDetails);
        return new ApiResponse<>("태스크가 수정되었습니다",null);
    }
}

