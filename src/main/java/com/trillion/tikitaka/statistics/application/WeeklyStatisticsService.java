package com.trillion.tikitaka.statistics.application;

import com.trillion.tikitaka.statistics.dto.response.WeeklyStatisticsResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeeklyStatisticsService {

    private final TicketRepository ticketRepository;

    /**
     * 요약 통계 조회
     * 매개변수로 현재 로그인 사용자의 managerId (User ID) 받아서 처리 (예시)
     */
    public WeeklyStatisticsResponse getWeeklySummary(Long managerId) {
        // 1) today 0시, tomorrow 0시
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        // 2) 금일 처리(DONE) 티켓 건수
        int dayTickets = ticketRepository.countDoneBetweenAndManager(
                startOfToday, endOfToday, managerId);

        // 3) 금일 긴급 티켓 건수 = urgent = true, createdAt today, manager=?
        int dayUrgentTickets = ticketRepository.countUrgentCreatedToday(
                startOfToday, endOfToday, managerId);

        // 4) 금주(월~금) 처리 건수
        //   이번 주의 월요일 0시 ~ 금요일 23:59:59 (또는 토요일 0시)
        LocalDateTime mondayStart = getMondayOfThisWeek().atStartOfDay();
        LocalDateTime saturdayStart = mondayStart.plusDays(5); // 토요일 0시

        int weekTickets = ticketRepository.countDoneBetweenAndManager(
                mondayStart, saturdayStart, managerId);

        // 5) 요일별 DONE 티켓 처리건수 (월~일)
        //   updatedAt each day, manager=?
        //   ex) "Mon": x, "Tue": y ...
        Map<String, Integer> weeklyTicketCounts = new LinkedHashMap<>();
        weeklyTicketCounts.put("Mon", 0);
        weeklyTicketCounts.put("Tue", 0);
        weeklyTicketCounts.put("Wed", 0);
        weeklyTicketCounts.put("Thu", 0);
        weeklyTicketCounts.put("Fri", 0);
        weeklyTicketCounts.put("Sat", 0);
        weeklyTicketCounts.put("Sun", 0);

        // 날짜별로 쿼리를 날리거나, 일단 한주 전체(DONE + manager) 가져와서 분류:
        // 여기서는 간단히 "월~일" 0시부터 24시까지 7번 쿼리 예시
        for (DayOfWeek day : DayOfWeek.values()) {
            // Monday(1) ~ Sunday(7)
            LocalDateTime dayStart = getMondayOfThisWeek().plusDays(day.getValue()-1).atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            int dailyCount = ticketRepository.countDoneBetweenAndManager(dayStart, dayEnd, managerId);
            weeklyTicketCounts.put(day.name().substring(0,3).substring(0,1).toUpperCase()
                            + day.name().substring(1,3).toLowerCase(),
                    dailyCount);
            // 예: Monday -> "Mon", Tuesday -> "Tue" ...
        }

        // 실제로 "Mon","Tue","Wed","Thu","Fri","Sat","Sun" 순서가 필요하면
        // 위처럼 LinkedHashMap + 루프 제어

        return new WeeklyStatisticsResponse(
                weeklyTicketCounts,
                dayTickets,
                dayUrgentTickets,
                weekTickets
        );
    }

    /**
     * 이번 주의 월요일(LocalDate) 구하기
     */
    private LocalDate getMondayOfThisWeek() {
        LocalDate today = LocalDate.now();
        // dayOfWeek: MONDAY(1) ~ SUNDAY(7)
        return today.minusDays(today.getDayOfWeek().getValue()- DayOfWeek.MONDAY.getValue());
    }
}
