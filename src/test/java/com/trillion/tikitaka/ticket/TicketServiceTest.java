package com.trillion.tikitaka.ticket;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketResponse;
import com.trillion.tikitaka.ticket.exception.UnauthorizedTicketAccessException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("티켓 유닛 테스트")
class TicketServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TicketService ticketService;

    private CustomUserDetails userDetailsUser;
    private CustomUserDetails userDetailsManager;
    private CustomUserDetails userDetailsAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(100L);
        when(mockUser.getRole()).thenReturn(Role.USER);
        when(mockUser.getUsername()).thenReturn("user");
        userDetailsUser = new CustomUserDetails(mockUser);

        User mockManager = mock(User.class);
        when(mockManager.getId()).thenReturn(200L);
        when(mockManager.getRole()).thenReturn(Role.MANAGER);
        when(mockManager.getUsername()).thenReturn("manager");
        userDetailsManager = new CustomUserDetails(mockManager);

        User mockAdmin = mock(User.class);
        when(mockAdmin.getId()).thenReturn(300L);
        when(mockAdmin.getRole()).thenReturn(Role.ADMIN);
        when(mockAdmin.getUsername()).thenReturn("admin");
        userDetailsAdmin = new CustomUserDetails(mockAdmin);
    }

//    @Nested
//    @DisplayName("Ticket 생성 성공 케이스")
//    class CreateTicketSuccess {
//
//        @Test
//        @DisplayName("모든 값이 올바르게 주어졌을 때 Ticket 생성에 성공한다.")
//        void should_CreateTicket_When_AllFieldsAreValid() {
//            // given
//            Long validManagerId = null;
//            CreateTicketRequest request = CreateTicketRequest.builder()
//                    .title("Test Ticket")
//                    .description("Test description")
//                    .urgent(true)
//                    .typeId(1L)
//                    .firstCategoryId(1L)
//                    .secondCategoryId(2L)
//                    .deadline(LocalDateTime.now().plusDays(1))
//                    .requesterId(3L)
//                    .managerId(validManagerId)
//                    .build();
//
//            // Mock 설정
//            //when(userRepository.existsById(validManagerId)).thenReturn(true);
//            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            ticketService.createTicket(request);
//
//            // then
//            verify(userRepository, times(1)).existsById(validManagerId); // 매니저 ID 확인
//            verify(ticketRepository, times(1)).save(any(Ticket.class)); // Ticket 저장 확인
//        }
//
//        @Test
//        @DisplayName("담당자가 지정되지 않은 경우 기본 매니저 ID로 생성된다.")
//        void should_CreateTicket_WithDefaultManagerId_When_ManagerIdIsNull() {
//            // given
//            CreateTicketRequest request = CreateTicketRequest.builder()
//                    .title("Test Ticket")
//                    .description("Test description")
//                    .urgent(false)
//                    .typeId(1L)
//                    .firstCategoryId(1L)
//                    .secondCategoryId(2L)
//                    .deadline(LocalDateTime.now().plusDays(1))
//                    .requesterId(100L)
//                    .build();
//
//            // 기본 매니저 ID가 2L로 설정된다고 가정
//            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
//                Ticket ticket = invocation.getArgument(0);
//                assertThat(ticket.getManagerId()).isEqualTo(2L); // 기본 매니저 ID 확인
//                return ticket;
//            });
//
//            // when
//            ticketService.createTicket(request);
//
//            // then
//            verify(ticketRepository, times(1)).save(any(Ticket.class));
//        }
//    }
//
//    @Nested
//    @DisplayName("Ticket 생성 실패 케이스")
//    class CreateTicketFailure {
//        // 실패 케이스 테스트 코드 (동일)
//    }
//
//    @Nested
//    @DisplayName("Ticket 생성 시 예외 처리")
//    class HandleCreateTicketException {
//        // 예외 처리 테스트 코드 (동일)
//    }

    @Nested
    @DisplayName("티켓 상태별 건수 조회 테스트")
    class DescribeCountTicketsByStatus {

        @Test
        @DisplayName("일반 사용자라면 본인이 요청한 티켓의 상태별 건수를 조회한다.")
        void should_ReturnSelfCreatedTicketCount_when_RoleIsUser() {
            // given
            TicketCountByStatusResponse mockCount = new TicketCountByStatusResponse(10L, 2L, 3L, 1L, 4L, 2L);

            when(ticketRepository.countTicketsByStatus(100L, "USER"))
                    .thenReturn(mockCount);

            // when
            TicketCountByStatusResponse result = ticketService.countTicketsByStatus(userDetailsUser);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(10L);
        }

        @Test
        @DisplayName("담당자나 관리자라면 전체 티켓의 상태별 건수를 조회한다.")
        void should_ReturnTotalTicketCount_when_RoleIsManagerOrAdmin() {
            // given
            TicketCountByStatusResponse mockCount = new TicketCountByStatusResponse(30L, 5L, 10L, 5L, 10L, 3L);

            when(ticketRepository.countTicketsByStatus(null, "MANAGER"))
                    .thenReturn(mockCount);

            // when
            TicketCountByStatusResponse result = ticketService.countTicketsByStatus(userDetailsManager);

            // then
            assertThat(result.getTotal()).isEqualTo(30L);
        }
    }

    @Nested
    @DisplayName("티켓 목록 조회 테스트")
    class DescribeGetTicketList {

        @Test
        @DisplayName("유효한 필터링 조건이 주어지면 티켓 목록을 정상적으로 조회한다.")
        void should_GetTicketList_when_ValidRequestGiven() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<TicketListResponse> mockPage = new PageImpl<>(List.of());

            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(ticketTypeRepository.existsById(anyLong())).thenReturn(true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(secondCategory));
            when(secondCategory.isChildOf(firstCategory)).thenReturn(true);
            when(userRepository.existsById(anyLong())).thenReturn(true);
            when(ticketRepository.getTicketList(
                    pageable,
                    Ticket.Status.PENDING,
                    1L,
                    2L,
                    1L,
                    null,
                    100L,
                    "USER"
            )).thenReturn(mockPage);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable,
                    Ticket.Status.PENDING,
                    1L,
                    2L,
                    1L,
                    null,
                    null,
                    userDetailsUser
            );

            // then
            assertThat(result).isEmpty();
            verify(ticketRepository, times(1)).getTicketList(
                    pageable, Ticket.Status.PENDING, 1L, 2L, 1L, null, 100L, "USER"
            );
        }

        @Test
        @DisplayName("일반 사용자가 담당자별 티켓 목록 조회를 요청하면 오류가 발생한다.")
        void should_ThrowException_when_UserRequestsManagerTicketListFilter() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Long managerId = 200L;

            // when & then
            assertThatThrownBy(() -> ticketService.getTicketList(
                    pageable,
                    Ticket.Status.PENDING,
                    1L,
                    2L,
                    1L,
                    managerId,
                    null,
                    userDetailsUser
            )).isInstanceOf(UnauthorizedTicketAccessException.class);
        }

        @Test
        @DisplayName("일반 사용자는 항상 본인이 요청한 티켓 목록을 조회한다.")
        void should_ReturnSelfRequestedTicketList_when_UserRequestsTicketList() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<TicketListResponse> mockPage = new PageImpl<>(List.of());

            when(ticketTypeRepository.existsById(anyLong())).thenReturn(true);
            when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mock(Category.class)));
            when(userRepository.existsById(anyLong())).thenReturn(true);
            when(ticketRepository.getTicketList(
                    pageable,
                    null,
                    null,
                    null,
                    null,
                    null,
                    100L,
                    "USER"
            )).thenReturn(mockPage);

            // when
            Page<TicketListResponse> result = ticketService.getTicketList(
                    pageable,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    userDetailsUser
            );

            // then
            assertThat(result).isEmpty();
            verify(ticketRepository, times(1)).getTicketList(
                    pageable, null, null, null, null, null, 100L, "USER"
            );
        }

        @Test
        @DisplayName("유효하지 않은 티켓 타입이 주어지면 오류가 발생한다.")
        void should_ThrowException_when_InvalidTicketTypeIsGiven() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(ticketTypeRepository.existsById(999L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> ticketService.getTicketList(
                    pageable,
                    null,
                    null,
                    1L,
                    999L,
                    null,
                    null,
                    userDetailsManager
            )).isInstanceOf(TicketTypeNotFoundException.class);
        }

        @Test
        @DisplayName("유효하지 않은 카테고리가 주어지면 오류가 발생한다.")
        void should_ThrowException_when_InvalidCategoryIsGiven() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(ticketTypeRepository.existsById(anyLong())).thenReturn(true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
            when(userRepository.existsById(anyLong())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> ticketService.getTicketList(
                    pageable,
                    null,
                    1L,
                    2L,
                    5L,
                    null,
                    null,
                    userDetailsManager
            )).isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("카테고리 관계가 유효하지 않으면 오류가 발생한다.")
        void should_ThrowException_when_CategoryRelationshipIsInvalid() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Category firstCategory = mock(Category.class);
            Category secondCategory = mock(Category.class);

            when(ticketTypeRepository.existsById(anyLong())).thenReturn(true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(firstCategory));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(secondCategory));
            when(secondCategory.isChildOf(firstCategory)).thenReturn(false);
            when(userRepository.existsById(anyLong())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> ticketService.getTicketList(
                    pageable,
                    null,
                    1L,
                    2L,
                    5L,
                    null,
                    null,
                    userDetailsManager
            )).isInstanceOf(InvalidCategoryLevelException.class);
        }

        @Test
        @DisplayName("유효하지 않은 담당자나 사용자가 주어지면 오류가 발생한다.")
        void should_ThrowException_when_InvalidManagerOrUserIsGiven() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Category first = mock(Category.class);
            when(ticketTypeRepository.existsById(anyLong())).thenReturn(true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(first));
            when(userRepository.existsById(999L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> ticketService.getTicketList(
                    pageable,
                    null,
                    1L,
                    null,
                    null,
                    999L,
                    null,
                    userDetailsManager
            )).isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("티켓 상세 조회 테스트")
    class DescribeGetTicket {

        @Test
        @DisplayName("사용자가 조회하면 우선순위를 null로 하여 티켓을 조회한다.")
        void should_GetTicketWithPriorityNull_when_UserRequestsTicket() {
            // given
            Long ticketId = 10L;
            TicketResponse mockResponse = new TicketResponse(
                    ticketId, "제목",
                    "내용",
                    Ticket.Priority.HIGH,
                    Ticket.Status.IN_PROGRESS,
                    "타입명",
                    "1차",
                    "2차",
                    "매니저",
                    "사용자",
                    false,
                    null,
                    null,
                    null,
                    null
            );
            when(ticketRepository.getTicket(ticketId, 100L, "USER"))
                    .thenReturn(mockResponse);

            // when
            TicketResponse result = ticketService.getTicket(ticketId, userDetailsUser);

            // then
            assertThat(result.getTicketId()).isEqualTo(ticketId);
            assertThat(result.getPriority()).isNull();
        }

        @Test
        @DisplayName("관리자나 담당자가 조회하면 우선순위를 포함하여 티켓을 조회한다.")
        void should_GetTicketWithPriority_when_ManagerOrAdminRequestsTicket() {
            // given
            Long ticketId = 10L;
            TicketResponse mockResponse = new TicketResponse(
                    ticketId, "제목",
                    "내용",
                    Ticket.Priority.HIGH,
                    Ticket.Status.IN_PROGRESS,
                    "타입명",
                    "1차",
                    "2차",
                    "매니저",
                    "사용자",
                    false,
                    null,
                    null,
                    null,
                    null
            );
            when(ticketRepository.getTicket(ticketId, 200L, "MANAGER"))
                    .thenReturn(mockResponse);
            when(ticketRepository.getTicket(ticketId, 300L, "ADMIN"))
                    .thenReturn(mockResponse);

            // when
            TicketResponse managerResult = ticketService.getTicket(ticketId, userDetailsManager);
            TicketResponse adminResult = ticketService.getTicket(ticketId, userDetailsAdmin);

            // then
            assertThat(managerResult.getPriority()).isEqualTo(Ticket.Priority.HIGH);
            assertThat(adminResult.getPriority()).isEqualTo(Ticket.Priority.HIGH);
        }
    }
}
