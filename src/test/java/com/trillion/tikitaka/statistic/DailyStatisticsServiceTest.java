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
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("📌 금일 티켓 처리 현황 조회")
    class DescribeGetDailySummary {

        @Test
        @DisplayName("금일 생성, 진행중, 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectDailyStatistics() {
            // given
            LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
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

            // verify
            verify(ticketRepository, times(1)).countCreatedToday(startOfToday, endOfToday);
            verify(ticketRepository, times(1)).countInProgressToday(startOfToday, endOfToday, Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW);
            verify(ticketRepository, times(1)).countDoneToday(startOfToday, endOfToday, Ticket.Status.DONE);
        }
    }
    @Nested
    @DisplayName("📌 금일 담당자별 티켓 처리 현황 조회")
    class DescribeGetDailyManagerSummary {

        @Test
        @DisplayName("✅ 금일 담당자별 진행 중 & 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectDailyManagerStatistics() {
            // given
            LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfToday = startOfToday.plusDays(1);

            // 가짜 사용자 리스트 (매니저 역할)
            List<UserResponse> mockUsers = List.of(
                    new UserResponse(101L, "김철수", "chulsoo@example.com", Role.MANAGER, ""),
                    new UserResponse(102L, "박영희", "younghee@example.com", Role.MANAGER, "")
            );

            // ✅ userService.findAllUsers()를 호출하는 대신, mockUsers를 직접 사용
            when(ticketRepository.countByManagerAndStatus(101L, startOfToday, endOfToday,
                    List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW))).thenReturn(5);
            when(ticketRepository.countByManagerAndStatus(102L, startOfToday, endOfToday,
                    List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW))).thenReturn(3);

            // 가짜 담당자별 완료된 티켓 수
            when(ticketRepository.countByManagerAndStatus(101L, startOfToday, endOfToday,
                    List.of(Ticket.Status.DONE))).thenReturn(8);
            when(ticketRepository.countByManagerAndStatus(102L, startOfToday, endOfToday,
                    List.of(Ticket.Status.DONE))).thenReturn(6);

            // ✅ 직접 리스트를 사용하여 결과 생성
            List<AllUser> managerStats = mockUsers.stream().map(user -> {
                int inProgressTickets = ticketRepository.countByManagerAndStatus(
                        user.getUserId(), startOfToday, endOfToday, List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW));
                int doneTickets = ticketRepository.countByManagerAndStatus(
                        user.getUserId(), startOfToday, endOfToday, List.of(Ticket.Status.DONE));

                AllUser allUser = new AllUser(); // ✅ 기본 생성자로 객체 생성
                allUser.updateAllUser(
                        user.getUsername(),
                        user.getEmail(),
                        user.getUserId(),
                        user.getProfileImageUrl(),
                        doneTickets,
                        inProgressTickets
                );
                return allUser;
            }).toList();

            // when
            List<AllUser> result = dailyStatisticsService.getDailyManagerSummary();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo(101L);
            assertThat(result.get(0).getDoneTickets()).isEqualTo(8);
            assertThat(result.get(0).getInProgressTickets()).isEqualTo(5);
            assertThat(result.get(1).getUserId()).isEqualTo(102L);
            assertThat(result.get(1).getDoneTickets()).isEqualTo(6);
            assertThat(result.get(1).getInProgressTickets()).isEqualTo(3);

            // verify
            verify(ticketRepository, times(1)).countByManagerAndStatus(101L, startOfToday, endOfToday,
                    List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW));
            verify(ticketRepository, times(1)).countByManagerAndStatus(102L, startOfToday, endOfToday,
                    List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW));
            verify(ticketRepository, times(1)).countByManagerAndStatus(101L, startOfToday, endOfToday,
                    List.of(Ticket.Status.DONE));
            verify(ticketRepository, times(1)).countByManagerAndStatus(102L, startOfToday, endOfToday,
                    List.of(Ticket.Status.DONE));
        }
    }
}
