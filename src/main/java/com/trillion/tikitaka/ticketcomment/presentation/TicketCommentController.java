package com.trillion.tikitaka.ticketcomment.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.ticketcomment.application.TicketCommentService;
import com.trillion.tikitaka.ticketcomment.dto.request.TicketCommentRequest;
import com.trillion.tikitaka.ticketcomment.dto.response.TicketCommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    @PostMapping("/{ticketId}/comments")
    public ApiResponse<Void> createTicketComment(@PathVariable("ticketId") Long ticketId,
                                                 @RequestBody @Valid TicketCommentRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        ticketCommentService.createTicketComment(ticketId, request, userDetails);
        return new ApiResponse<>(null);
    }

    @GetMapping("/{ticketId}/comments")
    public ApiResponse<List<TicketCommentResponse>> getTicketComments(@PathVariable("ticketId") Long ticketId,
                                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TicketCommentResponse> response = ticketCommentService.getTicketComments(ticketId, userDetails);
        return new ApiResponse<>(response);
    }
}
