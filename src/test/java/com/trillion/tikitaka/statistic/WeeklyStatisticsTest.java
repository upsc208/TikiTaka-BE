package com.trillion.tikitaka.statistic;

import com.trillion.tikitaka.statistics.application.WeeklyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.WeeklyStatisticsResponse;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
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

import static org.mockito.Mockito.lenient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("주간 통계 서비스 테스트")
class WeeklyStatisticsTest {

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
    @DisplayName("주간 요약 통계")
    class GetWeeklySummary {

        @Test
        @DisplayName("케이스 1 - 주간 완료된 티켓 수 검증")
        void shouldReturnWeeklyStatistics_Case1() {
            testWeeklyStatistics(new int[]{3, 4, 5, 6, 7, 2, 1}, 5, 2, 28);
        }

        @Test
        @DisplayName("케이스 2 - 주간 완료된 티켓 수 검증")
        void shouldReturnWeeklyStatistics_Case2() {
            testWeeklyStatistics(new int[]{2, 3, 4, 5, 6, 7, 8}, 4, 1, 35);
        }

        @Test
        @DisplayName("케이스 3 - 주간 완료된 티켓 수 검증")
        void shouldReturnWeeklyStatistics_Case3() {
            testWeeklyStatistics(new int[]{10, 5, 8, 3, 4, 7, 6}, 8, 3, 43);
        }

        private void testWeeklyStatistics(int[] dailyCounts, int expectedDayTickets, int expectedUrgentTickets, int expectedWeekTickets) {
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
                        .thenReturn(expectedDayTickets);
                lenient().when(ticketRepository.countUrgentCreatedToday(eq(todayStart), eq(todayEnd), eq(managerId)))
                        .thenReturn(expectedUrgentTickets);
                lenient().when(ticketRepository.countDoneBetweenAndManager(eq(mondayStart), eq(saturdayStart), eq(managerId)))
                        .thenReturn(expectedWeekTickets);

                int calculatedSum = 0;
                for (int i = 0; i < dailyCounts.length; i++) {
                    lenient().when(ticketRepository.countDoneBetweenAndManager(eq(mondayStart.plusDays(i)), eq(mondayStart.plusDays(i + 1)), eq(managerId)))
                            .thenReturn(dailyCounts[i]);
                    calculatedSum += dailyCounts[i];
                }

                assertThat(calculatedSum).isEqualTo(expectedWeekTickets);

                WeeklyStatisticsResponse response = weeklyStatisticsService.getWeeklySummary(mockUserDetails);

                System.out.println("테스트 케이스");
                System.out.println("오늘 완료된 티켓 수: " + response.getDayTickets());
                System.out.println("오늘 긴급 티켓 수: " + response.getDayUrgentTickets());
                System.out.println("주간 완료된 티켓 수: " + response.getWeekTickets());

                assertThat(response).isNotNull();
                assertThat(response.getDayTickets()).isEqualTo(expectedDayTickets);
                assertThat(response.getDayUrgentTickets()).isEqualTo(expectedUrgentTickets);
                assertThat(response.getWeekTickets()).isEqualTo(expectedWeekTickets);

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
