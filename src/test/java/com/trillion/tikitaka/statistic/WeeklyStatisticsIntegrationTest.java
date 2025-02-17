package com.trillion.tikitaka.statistic;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.statistics.application.WeeklyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.WeeklyStatisticsResponse;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("주간 통계 서비스 통합 테스트")
class WeeklyStatisticsIntegrationTest {

    @InjectMocks
    private WeeklyStatisticsService weeklyStatisticsService;

    @Mock
    private TicketRepository ticketRepository;

    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        User mockUser = new User(100L, "testUser", "USER");
        mockUserDetails = new CustomUserDetails(mockUser);
    }

    @Nested
    @DisplayName("주간 요약 통계 통합 테스트")
    class GetWeeklySummaryIntegration {

        @Test
        @DisplayName("주간 요약 데이터를 정확히 반환해야 한다")
        void shouldReturnCorrectWeeklyStatistics() {
            Long managerId = 100L;
            LocalDate fixedDate = LocalDate.of(2025, 1, 1);
            LocalDate expectedMonday = fixedDate.with(DayOfWeek.MONDAY);

            try (MockedStatic<LocalDate> mockedDate = Mockito.mockStatic(LocalDate.class)) {
                mockedDate.when(LocalDate::now).thenReturn(fixedDate);
                mockedDate.when(() -> fixedDate.with(DayOfWeek.MONDAY)).thenReturn(expectedMonday);

                LocalDate productionMonday = fixedDate.with(DayOfWeek.MONDAY);
                assertThat(productionMonday).isEqualTo(expectedMonday);

                LocalDateTime todayStart = fixedDate.atStartOfDay();
                LocalDateTime todayEnd = todayStart.plusDays(1);
                LocalDateTime mondayStart = productionMonday.atStartOfDay();
                LocalDateTime saturdayStart = mondayStart.plusDays(5);

                lenient().when(ticketRepository.countDoneBetweenAndManager(eq(todayStart), eq(todayEnd), eq(managerId)))
                        .thenReturn(5);
                lenient().when(ticketRepository.countUrgentCreatedToday(eq(todayStart), eq(todayEnd), eq(managerId)))
                        .thenReturn(2);
                lenient().when(ticketRepository.countDoneBetweenAndManager(eq(mondayStart), eq(saturdayStart), eq(managerId)))
                        .thenReturn(20);

                int[] dailyCounts = {3, 4, 5, 6, 7, 2, 1};
                for (int i = 0; i < dailyCounts.length; i++) {
                    lenient().when(ticketRepository.countDoneBetweenAndManager(eq(mondayStart.plusDays(i)), eq(mondayStart.plusDays(i + 1)), eq(managerId)))
                            .thenReturn(dailyCounts[i]);
                }

                WeeklyStatisticsResponse response = weeklyStatisticsService.getWeeklySummary(mockUserDetails);

                assertThat(response).isNotNull();
                assertThat(response.getDayTickets()).isEqualTo(5);
                assertThat(response.getDayUrgentTickets()).isEqualTo(2);
                assertThat(response.getWeekTickets()).isEqualTo(20);

                verify(ticketRepository, atLeastOnce())
                        .countDoneBetweenAndManager(eq(todayStart), eq(todayEnd), eq(managerId));
                verify(ticketRepository, atLeastOnce())
                        .countUrgentCreatedToday(eq(todayStart), eq(todayEnd), eq(managerId));
                verify(ticketRepository, atLeastOnce())
                        .countDoneBetweenAndManager(eq(mondayStart), eq(saturdayStart), eq(managerId));
            }
        }
    }
}
