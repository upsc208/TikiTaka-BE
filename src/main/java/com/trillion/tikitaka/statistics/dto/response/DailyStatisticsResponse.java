package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyStatisticsResponse {
    private int createdTickets;
    private int inProgressTickets;
    private int doneTickets;
}
