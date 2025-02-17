package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class WeeklyStatisticsResponse {

    private Map<String, Integer> weeklyTicketCounts;

    private int dayTickets;

    private int dayUrgentTickets;

    private int weekTickets;
}
