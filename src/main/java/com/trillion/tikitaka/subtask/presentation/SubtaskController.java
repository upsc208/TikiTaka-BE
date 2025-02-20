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
        subtaskService.calculateProgress(request.getTicketId());
        return new ApiResponse<>("태스크가 생성되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/{ticketId}")
    public ApiResponse<List<SubtaskResponse>> getSubtask(@PathVariable Long ticketId){
        List<SubtaskResponse> subtasks = subtaskService.getSubtasksByTicketId(ticketId);
        return new ApiResponse<>(subtasks);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @DeleteMapping("/{ticketId}/{taskId}")
    public ApiResponse<Void> deleteSubtask(@PathVariable Long taskId,@PathVariable Long ticketId){

        subtaskService.deleteSubtask(taskId,ticketId);
        subtaskService.calculateProgress(ticketId);
        return new ApiResponse<>("태스크가 삭제되었습니다",null);
    }
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @DeleteMapping("{ticketId}")
    public ApiResponse<Void> deleteAllSubtask(@PathVariable Long ticketId){

        subtaskService.deleteAllSubtask(ticketId);

        return new ApiResponse<>("태스크가 삭제되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/{taskId}")
    public ApiResponse<Void> editSubtask(@PathVariable Long taskId, @RequestBody SubtaskRequest request){

        subtaskService.editSubtask(taskId,request);

        return new ApiResponse<>("태스크가 수정되었습니다",null);
    }
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @PatchMapping("/{ticketId}/{taskId}/{checkBoolean}")
    public ApiResponse<Void> checkSubtask(@PathVariable Long ticketId,@PathVariable Long taskId,@PathVariable Boolean checkBoolean){

        subtaskService.updateSubtaskIsDone(taskId,checkBoolean,ticketId);
        subtaskService.calculateProgress(ticketId);
        return new ApiResponse<>("하위태스크의 수행상태가 변경되었습니다",null);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER','USER')")
    @GetMapping("/progress/{ticketId}")
    public ApiResponse<Double> getProgress(@PathVariable Long ticketId){
        Double progress = subtaskService.calculateProgress(ticketId);
        return new ApiResponse<>(progress);
    }

}

