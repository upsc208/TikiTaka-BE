package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyTypeStatisticsResponse {
    private Long ticketTypeId;
    private String ticketTypeName;
    private int ticketCount;
}
