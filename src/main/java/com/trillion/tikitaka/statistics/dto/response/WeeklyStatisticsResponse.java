package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class WeeklyStatisticsResponse {

    // 요일별 티켓 처리 건수 => 예: {"Mon": 5, "Tue": 3, ...}
    private Map<String, Integer> weeklyTicketCounts;

    // 금일 티켓 처리 건수
    private int dayTickets;

    // 금일 긴급 티켓 건수
    private int dayUrgentTickets;

    // 금주 티켓 처리 건수
    private int weekTickets;
}
