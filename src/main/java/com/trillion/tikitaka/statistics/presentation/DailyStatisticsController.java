package com.trillion.tikitaka.statistics.presentation;

import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.application.DailyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.DailyManagerStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyTypeStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.trillion.tikitaka.statistics.dto.AllUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics/daily")
@RequiredArgsConstructor
public class DailyStatisticsController {

    private final DailyStatisticsService dailyStatisticsService;

    /**
     * GET /statistics/daily/summary
     * - 금일 생성/진행중/완료된 티켓 통계
     */
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/summary")
    public ApiResponse<DailyStatisticsResponse> getDailySummary() {
        DailyStatisticsResponse data = dailyStatisticsService.getDailySummary();
        return ApiResponse.success(data);
    }

    /**
     * ✅ 일간 담당자별 티켓 처리 현황 조회 API (AllUser DTO 활용)
     */
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/manSummary")
    public ApiResponse<List<AllUser>> getDailyManagerSummary() {
        List<AllUser> managerStats = dailyStatisticsService.getDailyManagerSummary();
        return ApiResponse.success(managerStats);
    }

    /**
     * 📌 일간 유형별 티켓 생성 현황
     */
    @PreAuthorize("hasAnyAuthority('USER', 'MANAGER')")
    @GetMapping("/typeSummary")
    public ApiResponse<List<DailyTypeStatisticsResponse>> getDailyTypeSummary() {
        List<DailyTypeStatisticsResponse> response = dailyStatisticsService.getDailyTypeSummary();
        return ApiResponse.success(response);
    }
}
