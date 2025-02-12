package com.trillion.tikitaka.statistic;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.statistics.application.StatisticsService;
import com.trillion.tikitaka.statistics.domain.MonthlyStatistics;
import com.trillion.tikitaka.statistics.dto.response.AllCategory;
import com.trillion.tikitaka.statistics.dto.response.AllMonth;
import com.trillion.tikitaka.statistics.dto.response.AllType;
import com.trillion.tikitaka.statistics.dto.response.AllUser;
import com.trillion.tikitaka.statistics.infrastructure.MonthlyStatisticsRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.application.TicketTypeService;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@DisplayName("📊 월간 통계 서비스 테스트")
class StatisticsServiceTest {

    @InjectMocks
    private StatisticsService statisticsService;

    @Mock
    private MonthlyStatisticsRepository statisticsRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private TicketTypeService ticketTypeService;

    @Mock
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("🌍 월간 전체 티켓 통계 조회")
    class DescribeGetAllMonthlyTicket {

        @Test
        @DisplayName("✅ 해당 월의 생성, 긴급, 완료된 티켓 수를 반환한다")
        void should_ReturnCorrectMonthlyStatistics() {
            // given
            int year = 2025, month = 1;
            when(ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, null, null)).thenReturn(100);
            when(ticketRepository.countUrgentTicketsByCategoryAndUserAndType(year, month, null, null, null)).thenReturn(20);
            when(ticketRepository.countByCompletedStatusAndCategoryAndUserAndType(year, month, null, null, null)).thenReturn(80);

            // when
            AllMonth allMonth = statisticsService.getAllMonthlyTicket(year, month);

            // then
            assertThat(allMonth.getCreate()).isEqualTo(100);
            assertThat(allMonth.getUrgent()).isEqualTo(20);
            assertThat(allMonth.getComplete()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("📌 카테고리별 월간 통계 조회")
    class DescribeGetCategoryStatistics {

        @Test
        @DisplayName("✅ 모든 1차 및 2차 카테고리별 생성된 티켓 개수를 반환한다")
        void should_ReturnCategoryStatistics() {
            // given
            int year = 2025, month = 1;
            Category parentCategory = new Category("DevOps",  null);
            Category subCategory = new Category("CI/CD", parentCategory);

            when(categoryRepository.findAll()).thenReturn(List.of(parentCategory, subCategory));
            when(ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, parentCategory, null, null)).thenReturn(50);
            when(ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, subCategory, null, null)).thenReturn(30);

            // when
            List<AllCategory> result = statisticsService.getAllCategoryTicket(year, month);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFirstCategoryName()).isEqualTo("DevOps");
            assertThat(result.get(1).getFirstCategoryName()).isEqualTo("DevOps");
            assertThat(result.get(1).getSecondCategoryName()).isEqualTo("CI/CD");
        }
    }

    @Nested
    @DisplayName("🧑‍💻 유저별 월간 통계 조회")
    class DescribeGetUserStatistics {

        @Test
        @DisplayName("✅ MANAGER 역할을 가진 유저만 조회하고, 생성된 티켓 개수를 반환한다")
        void should_ReturnUserStatistics() {
            // given
            int year = 2025, month = 1;

            // ✅ UserResponse를 사용하여 유저 목록 생성
            UserResponse managerUserResponse = new UserResponse(1L, "ManagerA", "manager@example.com", Role.MANAGER, "profile_url");
            UserResponse normalUserResponse = new UserResponse(2L, "UserB", "user@example.com", Role.USER, "profile_url");

            // ✅ 실제 User 객체 생성
            User managerUser = new User(1L, "ManagerA", "MANAGER");
            User normalUser = new User(2L, "UserB", "USER");

            // ✅ userRepository가 올바르게 UserResponse 목록을 반환하도록 Mock 설정
            when(userRepository.getAllUsers()).thenReturn(List.of(managerUserResponse, normalUserResponse));

            // ✅ userRepository.findById()가 Optional<User>를 반환하도록 설정
            when(userRepository.findById(managerUserResponse.getUserId())).thenReturn(Optional.of(managerUser));
            when(userRepository.findById(normalUserResponse.getUserId())).thenReturn(Optional.of(normalUser));

            // ✅ MANAGER 역할을 가진 유저의 티켓 개수 Mock 설정
            when(ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, managerUser, null))
                    .thenReturn(40);

            // when
            List<AllUser> result = statisticsService.getAllUserTicket(year, month);

            // then
            assertThat(result).hasSize(1);  // ✅ MANAGER 유저만 포함되었는지 확인
            assertThat(result.get(0).getUserName()).isEqualTo("ManagerA");
            assertThat(result.get(0).getTotalManagingCreatedTicket()).isEqualTo(40);
        }


    }

    @Nested
    @DisplayName("📝 티켓 유형별 월간 통계 조회")
    class DescribeGetTypeStatistics {

        @Test
        @DisplayName("✅ 모든 티켓 유형별 생성된 티켓 개수를 반환한다")
        void should_ReturnTypeStatistics() {
            // given
            int year = 2025, month = 1;

            // ✅ TicketTypeListResponse를 사용하여 모의 데이터 생성
            TicketTypeListResponse bugFixResponse = new TicketTypeListResponse(1L, "Bug Fix");
            TicketTypeListResponse featureResponse = new TicketTypeListResponse(2L, "Feature");

            // ✅ 실제 TicketType 객체 생성
            TicketType bugFix = new TicketType(1L, "Bug Fix");
            TicketType feature = new TicketType(2L, "Feature");

            // ✅ TicketTypeService에서 목록을 가져오도록 설정
            when(ticketTypeService.getTicketTypes()).thenReturn(List.of(bugFixResponse, featureResponse));

            // ✅ TicketTypeRepository에서 ID로 TicketType을 찾도록 설정
            when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(bugFix));
            when(ticketTypeRepository.findById(2L)).thenReturn(Optional.of(feature));

            // ✅ TicketRepository에서 개수 반환하도록 설정
            when(ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, null, bugFix)).thenReturn(25);
            when(ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, null, feature)).thenReturn(50);

            // when
            List<AllType> result = statisticsService.getAllTypeTicket(year, month);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTypeName()).isEqualTo("Bug Fix");
            assertThat(result.get(0).getTotalCreated()).isEqualTo(25);
            assertThat(result.get(1).getTypeName()).isEqualTo("Feature");
            assertThat(result.get(1).getTotalCreated()).isEqualTo(50);
        }
    }

}
