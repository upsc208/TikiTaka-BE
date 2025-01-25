package com.trillion.tikitaka.ticket;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@DisplayName("Ticket 생성 테스트")
class TicketServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    public TicketServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Ticket 생성 성공 케이스")
    class CreateTicketSuccess {

        @Test
        @DisplayName("모든 값이 올바르게 주어졌을 때 Ticket 생성에 성공한다.")
        void should_CreateTicket_When_AllFieldsAreValid() {
            // given
            Long validManagerId = 3L;
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("Test description")
                    .urgent(true)
                    .typeId(1L)
                    .firstCategoryId(1L)
                    .secondCategoryId(2L)
                    .deadline(LocalDateTime.now().plusDays(1))
                    .requesterId(2L)
                    .managerId(validManagerId)
                    .priority(Ticket.Priority.HIGH)
                    .build();

            // Mock: managerId가 유효한 경우 true 반환
            when(userRepository.existsById(validManagerId)).thenReturn(true);

            // Mock: Ticket 저장 동작 정의
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ticketService.createTicket(request);

            // then
            verify(userRepository, times(1)).existsById(validManagerId); // 유효성 검사 호출 확인
            verify(ticketRepository, times(1)).save(any(Ticket.class)); // Ticket 저장 확인
        }


        @Test
        @DisplayName("담당자가 지정되지 않은 경우 기본 매니저 ID로 생성된다.")
        void should_CreateTicket_WithDefaultManagerId_When_ManagerIdIsNull() {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("Test description")
                    .urgent(false)
                    .typeId(1L)
                    .firstCategoryId(1L)
                    .secondCategoryId(2L)
                    .deadline(LocalDateTime.now().plusDays(1))
                    .requesterId(100L)
                    .priority(Ticket.Priority.MIDDLE)
                    .build();

            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
                Ticket ticket = invocation.getArgument(0);
                assertThat(ticket.getManagerId()).isEqualTo(2L); // 기본 매니저 ID 확인
                return ticket;
            });

            // when
            ticketService.createTicket(request);

            // then
            verify(ticketRepository, times(1)).save(any(Ticket.class));
        }
    }


    @Nested
    @DisplayName("Ticket 생성 실패 케이스")
    class CreateTicketFailure {

        @Test
        @DisplayName("잘못된 매니저 ID를 입력하면 예외가 발생한다.")
        void should_ThrowException_When_ManagerIdIsInvalid() {
            // given
            Long invalidManagerId = 999L; // 존재하지 않는 매니저 ID
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("This is a test ticket")
                    .urgent(false)
                    .typeId(1L)
                    .firstCategoryId(1L)
                    .secondCategoryId(2L)
                    .deadline(java.time.LocalDateTime.now().plusDays(1))
                    .requesterId(100L)
                    .managerId(invalidManagerId) // 잘못된 매니저 ID
                    .priority(Ticket.Priority.HIGH)
                    .build();

            when(userRepository.existsById(invalidManagerId)).thenReturn(false); // 매니저 ID가 존재하지 않음

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.INVALID_TICKET_MANAGER.getMessage());

            verify(userRepository, times(1)).existsById(invalidManagerId); // 유효성 검사 확인
            verify(ticketRepository, never()).save(any(Ticket.class)); // 티켓 저장되지 않음
        }

        @Test
        @DisplayName("필수 필드가 누락된 경우 예외를 반환한다.")
        void should_ThrowException_When_RequiredFieldsAreMissing() {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("title") // 필수 필드 누락
                    .description("Test description")
                    .typeId(1L)
                    .deadline(LocalDateTime.now().plusDays(1))
                    .requesterId(null)
                    .priority(Ticket.Priority.LOW)
                    .build();

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(request))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("잘못된 타입의 ID가 주어진 경우 예외를 반환한다.")
        void should_ThrowException_When_InvalidTypeIdIsProvided() {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("Test description")
                    .typeId(-1L) // 잘못된 ID
                    .deadline(LocalDateTime.now().plusDays(1))
                    .requesterId(100L)
                    .priority(Ticket.Priority.HIGH)
                    .build();

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("잘못된 값을 입력하셨습니다.");
        }

        @Test
        @DisplayName("마감 기한이 과거일 경우 예외를 반환한다.")
        void should_ThrowException_When_DeadlineIsInThePast() {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("Test description")
                    .typeId(1L)
                    .deadline(LocalDateTime.now().minusDays(1)) // 과거 날짜
                    .requesterId(100L)
                    .priority(Ticket.Priority.MIDDLE)
                    .build();

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("마감기한이 잘못되었습니다.");
        }
    }

    @Nested
    @DisplayName("Ticket 생성 시 예외 처리")
    class HandleCreateTicketException {

        @Test
        @DisplayName("데이터베이스 저장 중 오류 발생 시 INTERNAL_SERVER_ERROR를 반환한다.")
        void should_ReturnInternalServerError_When_DatabaseSaveFails() {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("Test description")
                    .typeId(1L)
                    .deadline(LocalDateTime.now().plusDays(1))
                    .requesterId(100L)
                    .priority(Ticket.Priority.LOW)
                    .build();

            doThrow(new RuntimeException("데이터베이스 에러"))
                    .when(ticketRepository).save(any(Ticket.class));

            // when & then
            assertThatThrownBy(() -> ticketService.createTicket(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("데이터베이스 에러");
        }
    }
}
