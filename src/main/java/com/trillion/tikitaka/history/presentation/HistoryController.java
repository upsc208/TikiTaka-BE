package com.trillion.tikitaka.history.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.history.HistoryService;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    @GetMapping
    public ApiResponse<Page<HistoryResponse>> getTicketList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "ticketTypeId", required = false) Long ticketTypeId,
            @RequestParam(value = "updatedById", required = false) Long updatedById,
            @RequestParam(value = "ticketId", required = false) Long ticketId,
            @RequestParam(value = "updateType", required = false) String updateType
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HistoryResponse> historyList = historyService.getHistory(pageable,updatedById,ticketId,updateType);
        return new ApiResponse<>(historyList);
    }
}
