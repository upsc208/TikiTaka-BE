package com.trillion.tikitaka.statistics.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.application.DailyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.DailyTypeStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyCategoryStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.AllDoneUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/statistics/daily")
@RequiredArgsConstructor
public class DailyStatisticsController {

    private final DailyStatisticsService dailyStatisticsService;

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/summary")
    public ApiResponse<DailyStatisticsResponse> getDailySummary() {
        DailyStatisticsResponse data = dailyStatisticsService.getDailySummary();
        return ApiResponse.success(data);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/manSummary")
    public ApiResponse<List<AllDoneUser>> getDailyManagerSummary() {
        List<AllDoneUser> managerStats = dailyStatisticsService.getDailyManagerSummary();
        return ApiResponse.success(managerStats);
    }

    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER', 'ADMIN')")
    @GetMapping("/typeSummary")
    public ApiResponse<List<DailyTypeStatisticsResponse>> getDailyTypeSummary() {
        List<DailyTypeStatisticsResponse> response = dailyStatisticsService.getDailyTypeSummary();
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping("/catSummary")
    public ApiResponse<List<DailyCategoryStatisticsResponse>> getDailyCategorySummary() {
        return ApiResponse.success(dailyStatisticsService.getDailyCategorySummary());
    }
}
