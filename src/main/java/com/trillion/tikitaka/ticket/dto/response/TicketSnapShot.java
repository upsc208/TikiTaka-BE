package com.trillion.tikitaka.ticket.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TicketSnapShot {

    private final Long id;
    private final String title;
    private final String firstCategoryName;
    private final String secondCategoryName;
    private final String ticketTypeName;
    private final String managerUsername;
}
