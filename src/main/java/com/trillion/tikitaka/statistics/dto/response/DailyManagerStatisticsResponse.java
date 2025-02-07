package com.trillion.tikitaka.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyManagerStatisticsResponse {
    private Long managerId;
    private String managerName;
    private String managerEmail;
    private String userProfile;
    private int doneTickets;
    private int inProgressTickets;
}
