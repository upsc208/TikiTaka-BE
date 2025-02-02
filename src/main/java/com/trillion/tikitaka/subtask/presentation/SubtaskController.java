package com.trillion.tikitaka.subtask.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.subtask.application.SubtaskService;
import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;
    private final SubtaskRepository subtaskRepository;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PostMapping
    public ApiResponse<Void> createSubtask(@RequestBody SubtaskRequest request) {
        Subtask subtask = subtaskService.createSubtask(request);
        subtaskRepository.save(subtask);
        return new ApiResponse<>("태스크가 생성되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/{ticketId}")
    public ApiResponse<List<SubtaskResponse>> getSubtask(@PathVariable Long ticketId){
        List<SubtaskResponse> subtasks = subtaskService.getSubtasksByTicketId(ticketId);
        return new ApiResponse<>(subtasks);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @DeleteMapping("{taskId}")
    public ApiResponse<Void> deleteSubtask(@PathVariable Long taskId){
        subtaskService.deleteSubtask(taskId);
        return new ApiResponse<>("태스크가 삭제되었습니다",null);
    }
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @DeleteMapping("{ticketId}")
    public ApiResponse<Void> deleteAllSubtask(@PathVariable Long ticketId){
        subtaskService.deleteSubtask(ticketId);
        return new ApiResponse<>("태스크가 삭제되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/{ticketId}/{taskId}")
    public ApiResponse<Void> editSubtask(@PathVariable Long ticketId, @PathVariable Long taskId, @RequestBody SubtaskRequest request){
        subtaskService.editSubtask(ticketId,taskId,request);
        return new ApiResponse<>("태스크가 수정되었습니다",null);
    }
}

