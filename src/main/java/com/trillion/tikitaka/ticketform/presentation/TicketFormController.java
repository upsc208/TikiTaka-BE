package com.trillion.tikitaka.ticketform.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.ticketform.application.TicketFormService;
import com.trillion.tikitaka.ticketform.dto.request.TicketFormRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets/forms")
@RequiredArgsConstructor
public class TicketFormController {

    private final TicketFormService ticketFormService;

    public ApiResponse<Void> createTicketForm(@PathVariable("firstCategoryId") Long firstCategoryId,
                                              @PathVariable("secondCategoryId") Long secondCategoryId,
                                              @RequestBody @Valid TicketFormRequest request) {
        ticketFormService.createTicketForm(firstCategoryId, secondCategoryId, request.getDescription());
        return new ApiResponse<>(null);
    }
}
