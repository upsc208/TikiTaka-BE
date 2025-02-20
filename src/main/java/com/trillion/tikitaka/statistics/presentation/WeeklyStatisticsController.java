package com.trillion.tikitaka.statistics.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.application.WeeklyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.WeeklyStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class WeeklyStatisticsController {

    private final WeeklyStatisticsService weeklyStatisticsService;

    @PreAuthorize("hasAnyAuthority('MANAGER')")
    @GetMapping("/weekly/summary")
    public ApiResponse<WeeklyStatisticsResponse> getWeeklySummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        WeeklyStatisticsResponse data = weeklyStatisticsService.getWeeklySummary(userDetails);
        return ApiResponse.success(data);
    }
}
