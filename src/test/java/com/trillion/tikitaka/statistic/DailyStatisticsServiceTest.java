package com.trillion.tikitaka.statistic;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.statistics.dto.response.DailyCategoryStatisticsResponse;
import com.trillion.tikitaka.statistics.application.DailyStatisticsService;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyTypeStatisticsResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.statistics.application.StatisticsService;
import com.trillion.tikitaka.statistics.dto.response.DailyCompletionResponse;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;


@DisplayName("일간 티켓 처리 현황 테스트")
class DailyStatisticsServiceTest {

    @InjectMocks
    private DailyStatisticsService dailyStatisticsService;

    @InjectMocks
    private StatisticsService statisticsService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

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
    @DisplayName("🌍 일간 담당자별 티켓 통계 조회")
    class DescribeGetDailyManagerSummary {

        @Test
        @DisplayName("✅ 각 담당자의 진행 중 및 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectManagerStatistics() {
            // given
            LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfToday = startOfToday.plusDays(1);

            UserResponse manager1 = new UserResponse(101L, "김철수", "chulsoo@example.com", Role.MANAGER, "profile1.jpg");
            UserResponse manager2 = new UserResponse(102L, "박영희", "younghee@example.com", Role.MANAGER, "profile2.jpg");


            when(userRepository.getAllUsers()).thenReturn(List.of(manager1, manager2));

            when(ticketRepository.countByManagerAndStatus(101L, startOfToday, endOfToday, List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW))).thenReturn(3);
            when(ticketRepository.countByManagerAndStatus(101L, startOfToday, endOfToday, List.of(Ticket.Status.DONE))).thenReturn(5);

            when(ticketRepository.countByManagerAndStatus(102L, startOfToday, endOfToday, List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW))).thenReturn(2);
            when(ticketRepository.countByManagerAndStatus(102L, startOfToday, endOfToday, List.of(Ticket.Status.DONE))).thenReturn(7);

            // when
            List<AllUser> response = dailyStatisticsService.getDailyManagerSummary();

            // then
            assertThat(response).hasSize(2);

            AllUser result1 = response.get(0);
            assertThat(result1.getUserId()).isEqualTo(101L);
            assertThat(result1.getUserName()).isEqualTo("김철수");
            assertThat(result1.getInProgressTickets()).isEqualTo(3);
            assertThat(result1.getDoneTickets()).isEqualTo(5);

            AllUser result2 = response.get(1);
            assertThat(result2.getUserId()).isEqualTo(102L);
            assertThat(result2.getUserName()).isEqualTo("박영희");
            assertThat(result2.getInProgressTickets()).isEqualTo(2);
            assertThat(result2.getDoneTickets()).isEqualTo(7);
        }
    }
    @Nested
    @DisplayName("✅ 일간 유형별 티켓 생성 수 조회")
    class DescribeGetDailyTypeSummary {

        @Test
        @DisplayName("📌 각 유형별 금일 생성된 티켓 개수를 반환한다")
        void should_ReturnCorrectDailyTypeStatistics() {
            // given
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

            TicketType bugReport = new TicketType("버그 리포트");
            TicketType featureRequest = new TicketType("기능 요청");

            List<TicketType> ticketTypes = Arrays.asList(bugReport, featureRequest);

            when(ticketTypeRepository.findAll()).thenReturn(ticketTypes);
            when(ticketRepository.countByCreatedAtBetweenAndTicketType(startOfDay, endOfDay, bugReport)).thenReturn(45);
            when(ticketRepository.countByCreatedAtBetweenAndTicketType(startOfDay, endOfDay, featureRequest)).thenReturn(32);

            // when
            List<DailyTypeStatisticsResponse> result = dailyStatisticsService.getDailyTypeSummary();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(DailyTypeStatisticsResponse::getTicketTypeName)
                    .containsExactlyInAnyOrder("버그 리포트", "기능 요청");
            assertThat(result).extracting(DailyTypeStatisticsResponse::getTicketCount)
                    .containsExactlyInAnyOrder(45, 32);

            // verify
            verify(ticketTypeRepository, times(1)).findAll();
            verify(ticketRepository, times(1)).countByCreatedAtBetweenAndTicketType(startOfDay, endOfDay, bugReport);
            verify(ticketRepository, times(1)).countByCreatedAtBetweenAndTicketType(startOfDay, endOfDay, featureRequest);
        }
    }
    @Nested
    @DisplayName("✅ 일간 1차, 2차 카테고리별 생성된 티켓 개수 조회")
    class DescribeGetDailyCategorySummary {

        @Test
        @DisplayName("✅ 금일 1차, 2차 카테고리별 생성된 티켓 수를 반환한다")
        void should_ReturnCorrectDailyCategoryStatistics() {
            // Given
            // ✅ ID를 포함한 새로운 테스트용 생성자 사용
            Category firstCategory1 = new Category(1L, "IT 서비스", null);
            Category firstCategory2 = new Category(2L, "고객 지원", null);

            Category secondCategory1 = new Category(101L, "백엔드 개발", firstCategory1);
            Category secondCategory2 = new Category(102L, "프론트엔드 개발", firstCategory1);
            Category secondCategory3 = new Category(201L, "일반 상담", firstCategory2);
            Category secondCategory4 = new Category(202L, "불만 접수", firstCategory2);


            // 1차 카테고리별 티켓 개수 Mock 설정
            when(ticketRepository.countByFirstCategoryToday(any(), any()))
                    .thenReturn(List.of(
                            new Object[]{firstCategory1, 43},
                            new Object[]{firstCategory2, 53}
                    ));

            // 2차 카테고리별 티켓 개수 Mock 설정
            when(ticketRepository.countBySecondCategoryToday(any(), any(), eq(firstCategory1)))
                    .thenReturn(List.of(
                            new Object[]{secondCategory1, 25},
                            new Object[]{secondCategory2, 18}
                    ));

            when(ticketRepository.countBySecondCategoryToday(any(), any(), eq(firstCategory2)))
                    .thenReturn(List.of(
                            new Object[]{secondCategory3, 32},
                            new Object[]{secondCategory4, 21}
                    ));

            // When
            List<DailyCategoryStatisticsResponse> result = dailyStatisticsService.getDailyCategorySummary();
            System.out.println("결과 값: " + result);

            // Then
            assertThat(result)
                    .isNotEmpty()
                    .allSatisfy(category -> assertThat(category.getFirstCategoryId()).isNotNull());

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    new DailyCategoryStatisticsResponse(
                            firstCategory1.getId(),
                            firstCategory1.getName(),
                            List.of(
                                    new DailyCategoryStatisticsResponse.SecondCategoryInfo(secondCategory1.getId(), secondCategory1.getName(), 25),
                                    new DailyCategoryStatisticsResponse.SecondCategoryInfo(secondCategory2.getId(), secondCategory2.getName(), 18)
                            ),
                            43
                    ),
                    new DailyCategoryStatisticsResponse(
                            firstCategory2.getId(),
                            firstCategory2.getName(),
                            List.of(
                                    new DailyCategoryStatisticsResponse.SecondCategoryInfo(secondCategory3.getId(), secondCategory3.getName(), 32),
                                    new DailyCategoryStatisticsResponse.SecondCategoryInfo(secondCategory4.getId(), secondCategory4.getName(), 21)
                            ),
                            53
                    )
            ));
        }
    }
    @Nested
    @DisplayName("📌 금일 완료된 티켓 현황 조회")
    class DescribeGetDailyCompletionStatistics {

        @Test
        @DisplayName("✅ 담당자의 금일 생성 및 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectCompletionStatistics() {
            // Given (오늘 날짜 및 담당자 ID 설정)
            Long managerId = 22L; // 테스트할 담당자 ID
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

            int createdTickets = 13; // 금일 생성된 티켓 수
            int doneTickets = 7;     // 금일 완료된 티켓 수

            when(ticketRepository.countByCreatedAtAndUserId(todayStart, todayEnd, managerId)).thenReturn(createdTickets);
            when(ticketRepository.countByCompletedAtAndUserId(todayStart, todayEnd, managerId)).thenReturn(doneTickets);

            // When (서비스 실행)
            DailyCompletionResponse response = statisticsService.getDailyCompletionStatistics(managerId);

            // Then (결과 검증)
            assertThat(response).isNotNull();
            assertThat(response.getCreatedTickets()).isEqualTo(createdTickets);
            assertThat(response.getDoneTickets()).isEqualTo(doneTickets);

            // Mock 검증 (해당 메서드가 한 번씩 호출되었는지 확인)
            verify(ticketRepository, times(1)).countByCreatedAtAndUserId(todayStart, todayEnd, managerId);
            verify(ticketRepository, times(1)).countByCompletedAtAndUserId(todayStart, todayEnd, managerId);
        }
    }
}