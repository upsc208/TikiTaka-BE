package com.trillion.tikitaka.statistics.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfiguration {
    private final  StatisticsService statisticsService;

    @Scheduled(cron = "0 0 1 * * *")
    public void runScheduledStatisticsUpdate() {
        int year = java.time.LocalDate.now().getYear();
        int month = java.time.LocalDate.now().getMonthValue();
        statisticsService.updateMonthlyStatistics(year, month);
        System.out.println(year+" "+month);
    }
}
