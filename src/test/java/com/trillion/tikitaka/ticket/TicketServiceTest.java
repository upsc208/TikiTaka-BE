package com.trillion.tikitaka.ticket;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Nested
    @DisplayName("Ticket 생성 성공 케이스")
    class CreateTicketSuccess {

        @Test
        @DisplayName("모든 값이 올바르게 주어졌을 때 Ticket 생성에 성공한다.")
        void should_CreateTicket_When_AllFieldsAreValid() {
            // given
            Long validManagerId = null;
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("Test Ticket")
                    .description("Test description")
                    .urgent(true)
                    .typeId(1L)
                    .firstCategoryId(1L)
                    .secondCategoryId(2L)
                    .deadline(LocalDateTime.now().plusDays(1))
                    .requesterId(3L)
                    .managerId(validManagerId)
                    .priority(Ticket.Priority.HIGH)
                    .build();

            // Mock 설정
            //when(userRepository.existsById(validManagerId)).thenReturn(true);
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ticketService.createTicket(request);

            // then
            verify(userRepository, times(1)).existsById(validManagerId); // 매니저 ID 확인
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

            // 기본 매니저 ID가 2L로 설정된다고 가정
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
        // 실패 케이스 테스트 코드 (동일)
    }

    @Nested
    @DisplayName("Ticket 생성 시 예외 처리")
    class HandleCreateTicketException {
        // 예외 처리 테스트 코드 (동일)
    }
}
