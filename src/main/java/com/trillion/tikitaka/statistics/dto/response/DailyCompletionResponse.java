package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyCompletionResponse {
    private final int createdTickets;
    private final int doneTickets;
}
