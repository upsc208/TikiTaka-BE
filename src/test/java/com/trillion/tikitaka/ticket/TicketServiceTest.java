package com.trillion.tikitaka.ticket;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.history.HistoryService;
import com.trillion.tikitaka.ticket.application.TicketService;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private HistoryService historyService;

    private User user;
    private CustomUserDetails userDetails;
    private TicketType ticketType;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        // ✅ User 객체를 생성하고 this.user에 할당
        this.user = new User(1L,"user", "USER");
        this.userDetails = new CustomUserDetails(this.user);

        // ✅ TicketType도 명확히 초기화
        this.ticketType = new TicketType(1L, "Default Type");

        this.ticket = Ticket.builder()
                .id(1L)
                .title("테스트 티켓")
                .description("테스트 설명")
                .ticketType(ticketType)
                .requester(user)  // ✅ 여기도 this.user 사용
                .status(Ticket.Status.PENDING)
                .deadline(LocalDateTime.now().plusDays(7))
                .build();

        // ✅ ID가 제대로 설정되었는지 확인
        System.out.println("BeforeEach 실행: user ID -> " + user.getId());
        System.out.println("BeforeEach 실행: ticketType ID -> " + ticketType.getId());
    }





    @Test
    @Transactional
    void should_CreateTicket_When_ValidRequest() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("버그 발생", "로그인이 안 됨", 1L, null, null, LocalDateTime.now().plusDays(3), null, false);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findById(eq(1L))).thenReturn(Optional.of(ticketType));



        Long ticketId = ticketService.createTicket(request, null, userDetails);
        System.out.println("ticketService.createTicket() 반환값 -> " + ticketId);

        // Then
        assertThat(ticketId).isEqualTo(1L);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }


    @Test
    void should_ThrowException_When_TitleIsBlank() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("", "설명", 1L, null, null, LocalDateTime.now().plusDays(3), null, false);

        // When & Then
        assertThatThrownBy(() -> ticketService.createTicket(request, null, new CustomUserDetails(user)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_ThrowException_When_TicketTypeNotFound() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("버그 발생", "로그인이 안 됨", 99L, null, null, LocalDateTime.now().plusDays(3), null, false);
        when(ticketTypeRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.createTicket(request, null, new CustomUserDetails(user)))
                .isInstanceOf(TicketTypeNotFoundException.class);
    }


    @Test
    @Transactional
    void should_EditTicket_When_ValidRequest() {
        // Given
        EditTicketRequest request = new EditTicketRequest("수정된 제목", "수정된 설명", null, null, null, false, LocalDateTime.now().plusDays(5));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // When
        ticketService.editTicket(request, 1L, new CustomUserDetails(user));

        // Then
        assertThat(ticket.getTitle()).isEqualTo("수정된 제목");
        assertThat(ticket.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    void should_ThrowException_When_TicketNotFound() {
        // Given
        EditTicketRequest request = new EditTicketRequest("수정된 제목", "수정된 설명", null, null, null, false, LocalDateTime.now().plusDays(5));
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.editTicket(request, 99L, new CustomUserDetails(user)))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void should_ThrowException_When_InvalidCategoryGiven() {
        // Given
        EditTicketRequest request = new EditTicketRequest("수정된 제목", "수정된 설명", null, 99L, null, false, LocalDateTime.now().plusDays(5));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.editTicket(request, 1L, new CustomUserDetails(user)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void should_ThrowException_When_DeadlineIsPast() {
        // Given
        EditTicketRequest request = new EditTicketRequest("수정된 제목", "수정된 설명", null, null, null, false, LocalDateTime.now().minusDays(1));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // When & Then
        assertThatThrownBy(() -> ticketService.editTicket(request, 1L, userDetails))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
