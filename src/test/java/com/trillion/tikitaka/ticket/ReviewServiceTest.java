package com.trillion.tikitaka.ticket;

import com.trillion.tikitaka.ticket.domain.Review;
import com.trillion.tikitaka.ticket.infrastructure.ReviewRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.exception.TicketReviewAlreadyDoneException;
import com.trillion.tikitaka.ticket.exception.TicketReviewNotRequiredException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.ticket.application.ReviewService;
import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;
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
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("📝 티켓 검토 서비스 유닛 테스트")
class TicketReviewServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService ticketReviewService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("🔍 티켓 검토 기능 테스트")
    class DescribeDoReview {

        @Test
        @DisplayName("✅ 검토가 필요한 티켓을 정상적으로 검토하면 저장된다")
        void should_SaveReview_When_TicketNeedsReview() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;

            Ticket ticket = mock(Ticket.class);
            User reviewer = mock(User.class);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticket.getStatus()).thenReturn(Ticket.Status.REVIEW);
            when(userRepository.findById(userId)).thenReturn(Optional.of(reviewer));
            when(reviewRepository.existsByTicketAndReviewer(ticket, reviewer)).thenReturn(false);

            // When
            ticketReviewService.doReview(ticketId, userId);

            // Then
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        @Test
        @DisplayName("✅ 검토가 필요 없는 티켓에 대해 예외를 발생시킨다.")
        void should_ThrowException_When_TicketDoesNotNeedReview() {
            // Given
            Long ticketId = 1L;
            Long reviewerId = 100L;

            Ticket ticket = mock(Ticket.class);
            when(ticket.getStatus()).thenReturn(Ticket.Status.DONE); // 검토가 필요 없는 상태

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            // When & Then
            assertThatThrownBy(() -> ticketReviewService.doReview(ticketId, reviewerId)) // ✅ 인스턴스 호출
                    .isInstanceOf(TicketReviewNotRequiredException.class)
                    .hasMessage("검토를 요청하지 않은 티켓입니다.");
        }



        @Test
        @DisplayName("❌ 이미 검토한 티켓을 다시 검토하면 예외가 발생한다")
        void should_ThrowException_When_ReviewAlreadyExists() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;

            Ticket ticket = mock(Ticket.class);
            User reviewer = mock(User.class);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticket.getStatus()).thenReturn(Ticket.Status.REVIEW);
            when(userRepository.findById(userId)).thenReturn(Optional.of(reviewer));
            when(reviewRepository.existsByTicketAndReviewer(ticket, reviewer)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> ticketReviewService.doReview(ticketId, userId))
                    .isInstanceOf(TicketReviewAlreadyDoneException.class)
                    .hasMessage("이미 검토한 티켓입니다.");

            verify(reviewRepository, never()).save(any(Review.class));
        }
    }

    @Nested
    @DisplayName("📌 검토 목록 조회 기능 테스트")
    class DescribeGetReviews {

        @Test
        @DisplayName("✅ 검토된 티켓 목록을 정상적으로 반환한다")
        void should_ReturnReviewList_When_TicketExists() {
            // Given
            Long ticketId = 1L;
            LocalDateTime now = LocalDateTime.now();

            List<ReviewListResponse> expectedReviews = List.of(
                    new ReviewListResponse(1L, "reviewer1", now),
                    new ReviewListResponse(2L, "reviewer2", now)
            );

            when(ticketRepository.existsById(ticketId)).thenReturn(true);
            when(reviewRepository.findAllByTicketId(ticketId)).thenReturn(expectedReviews);

            // When
            List<ReviewListResponse> actualReviews = ticketReviewService.getReviews(ticketId);

            // Then
            assertThat(actualReviews).isNotNull();
            assertThat(actualReviews).hasSize(2);
            assertThat(actualReviews).isEqualTo(expectedReviews);
        }


        @Test
        @DisplayName("✅ 존재하지 않는 티켓을 조회하면 예외를 발생시킨다.")
        void should_ThrowException_When_TicketDoesNotExist() {
            // Given
            Long nonExistentTicketId = 999L;

            when(ticketRepository.existsById(nonExistentTicketId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> ticketReviewService.getReviews(nonExistentTicketId))
                    .isInstanceOf(TicketNotFoundException.class)
                    .hasMessage("해당 티켓을 찾을 수 없습니다.");

        verify(reviewRepository, never()).findAllByTicketId(any());
        }
    }
}