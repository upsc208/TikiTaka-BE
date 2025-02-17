package com.trillion.tikitaka.statistics.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.statistics.dto.response.WeeklyStatisticsResponse;
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

    public WeeklyStatisticsResponse getWeeklySummary(CustomUserDetails userDetails) {
        Long managerId = userDetails.getId();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        int dayTickets = ticketRepository.countDoneBetweenAndManager(
                startOfToday, endOfToday, managerId);

        int dayUrgentTickets = ticketRepository.countUrgentCreatedToday(
                startOfToday, endOfToday, managerId);

        LocalDateTime mondayStart = getMondayOfThisWeek().atStartOfDay();
        LocalDateTime saturdayStart = mondayStart.plusDays(5);

        int weekTickets = ticketRepository.countDoneBetweenAndManager(
                mondayStart, saturdayStart, managerId);

        Map<String, Integer> weeklyTicketCounts = new LinkedHashMap<>();
        weeklyTicketCounts.put("Mon", 0);
        weeklyTicketCounts.put("Tue", 0);
        weeklyTicketCounts.put("Wed", 0);
        weeklyTicketCounts.put("Thu", 0);
        weeklyTicketCounts.put("Fri", 0);
        weeklyTicketCounts.put("Sat", 0);
        weeklyTicketCounts.put("Sun", 0);

        for (DayOfWeek day : DayOfWeek.values()) {
            LocalDateTime dayStart = getMondayOfThisWeek().plusDays(day.getValue()-1).atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            int dailyCount = ticketRepository.countDoneBetweenAndManager(dayStart, dayEnd, managerId);
            weeklyTicketCounts.put(day.name().substring(0,3).substring(0,1).toUpperCase()
                            + day.name().substring(1,3).toLowerCase(),
                    dailyCount);
        }

        return new WeeklyStatisticsResponse(
                weeklyTicketCounts,
                dayTickets,
                dayUrgentTickets,
                weekTickets
        );
    }

    private LocalDate getMondayOfThisWeek() {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue()- DayOfWeek.MONDAY.getValue());
    }
}
