package com.trillion.tikitaka.statistic;

import com.trillion.tikitaka.statistics.application.DailyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.trillion.tikitaka.statistics.dto.AllUser;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import java.util.List;


@DisplayName("일간 티켓 처리 현황 테스트")
class DailyStatisticsServiceTest {

    @InjectMocks
    private DailyStatisticsService dailyStatisticsService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("📌 금일 티켓 처리 현황 조회")
    class DescribeGetDailySummary {

        @Test
        @DisplayName("✅ 금일 생성, 진행중, 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectDailyStatistics() {
            // given
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            LocalDateTime endOfToday = startOfToday.plusDays(1);

            when(ticketRepository.countCreatedToday(startOfToday, endOfToday)).thenReturn(10);
            when(ticketRepository.countInProgressToday(startOfToday, endOfToday, Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW)).thenReturn(5);
            when(ticketRepository.countDoneToday(startOfToday, endOfToday, Ticket.Status.DONE)).thenReturn(7);

            // when
            DailyStatisticsResponse result = dailyStatisticsService.getDailySummary();

            // then
            assertThat(result.getCreatedTickets()).isEqualTo(10);
            assertThat(result.getInProgressTickets()).isEqualTo(5);
            assertThat(result.getDoneTickets()).isEqualTo(7);
        }
    }
    @Nested
    @DisplayName("📌 금일 담당자별 티켓 처리 현황 조회")
    class DescribeGetDailyManagerSummary {

        @Test
        @DisplayName("✅ 금일 담당자별 진행 중 및 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectDailyManagerSummary() {
            // given
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            LocalDateTime endOfToday = startOfToday.plusDays(1);

            UserResponse mockUserResponse = new UserResponse(1L, "manager", "manager@example.com",Role.MANAGER, "profile.jpg");

            when(userRepository.getAllUsers()).thenReturn(List.of(mockUserResponse));

            when(ticketRepository.countByManagerAndStatus(1L, startOfToday, endOfToday, List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW)))
                    .thenReturn(3);
            when(ticketRepository.countByManagerAndStatus(1L, startOfToday, endOfToday, List.of(Ticket.Status.DONE)))
                    .thenReturn(2);

            // when
            List<AllUser> result = dailyStatisticsService.getDailyManagerSummary();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(1L);
            assertThat(result.get(0).getInProgressTickets()).isEqualTo(3);
            assertThat(result.get(0).getDoneTickets()).isEqualTo(2);
        }

    }
}
