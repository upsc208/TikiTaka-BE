package com.trillion.tikitaka.statistics.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatisticsScheduler {

    private final StatisticsService statisticsService;

    /**
     * 매일 새벽(01:00)에 실행
     * - 현재 연도와 월을 기준으로 월별 통계를 갱신
     * - 예: 2025년 2월이면 2025/2
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateMonthlyStatisticsAtMidnight() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        log.info("[스케줄러] 월별 통계 업데이트 시작: {}-{}", year, month);
        statisticsService.updateMonthlyStatistics(year, month);
        log.info("[스케줄러] 월별 통계 업데이트 완료: {}-{}", year, month);
    }
}