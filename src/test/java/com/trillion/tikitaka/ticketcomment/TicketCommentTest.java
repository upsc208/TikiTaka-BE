
package com.trillion.tikitaka.ticketcomment;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticketcomment.application.TicketCommentService;
import com.trillion.tikitaka.ticketcomment.domain.TicketComment;
import com.trillion.tikitaka.ticketcomment.dto.request.TicketCommentRequest;
import com.trillion.tikitaka.ticketcomment.dto.response.TicketCommentResponse;
import com.trillion.tikitaka.ticketcomment.exception.TicketCommentNotFoundException;
import com.trillion.tikitaka.ticketcomment.exception.UnauthorizedTicketCommentException;
import com.trillion.tikitaka.ticketcomment.infrastructure.TicketCommentRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.attachment.application.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("티켓 댓글 유닛 테스트")
public class TicketCommentTest {

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TicketCommentService ticketCommentService;

    private CustomUserDetails userDetailsUser;
    private CustomUserDetails userDetailsManager;

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
    }

    @Nested
    @DisplayName("티켓 댓글 생성 테스트")
    class DescribeCreateTicketComment {

        @Test
        @DisplayName("정상적으로 댓글을 입력하고 작성하면 댓글이 생성된다.")
        void should_GetTicketComments_When_ValidInput() {
            // given
            Long ticketId = 1L;

            Ticket mockTicket = mock(Ticket.class);
            when(ticketRepository.findById(ticketId))
                    .thenReturn(Optional.of(mockTicket));
            when(mockTicket.canComment(any(User.class))).thenReturn(true);

            List<TicketCommentResponse> mockList = List.of(
                    new TicketCommentResponse(1L, 2L, "작성자", "댓글 테스트", null, null),
                    new TicketCommentResponse(2L, 2L, "작성자", "댓글 테스트", null, null)
            );
            when(ticketCommentRepository.getTicketComments(ticketId))
                    .thenReturn(mockList);

            // when
            List<TicketCommentResponse> result = ticketCommentService.getTicketComments(ticketId, userDetailsUser);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContent()).isEqualTo("댓글 테스트");
        }

        @Test
        @DisplayName("요청한 티켓이 존재하지 않으면 예외가 발생한다.")
        void should_ThrowException_When_TicketNotFound() {
            // given
            Long ticketId = 1L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 테스트");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));

            assertThatThrownBy(() -> ticketCommentService.createTicketComment(ticketId, request, mockFiles, userDetailsUser))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketCommentRepository, never()).save(any(TicketComment.class));
        }

        @Test
        @DisplayName("요청한 티켓에 댓글을 작성할 권한이 없으면 예외가 발생한다.")
        void should_ThrowException_When_UnauthorizedTicketComment() {

            Long ticketId = 1L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 테스트");

            Ticket mockTicket = mock(Ticket.class);
            when(ticketRepository.findById(ticketId))
                    .thenReturn(Optional.of(mockTicket));

            when(mockTicket.canComment(any(User.class))).thenReturn(false);

            List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));

            assertThatThrownBy(() -> ticketCommentService.createTicketComment(ticketId, request, mockFiles, userDetailsUser))
                    .isInstanceOf(UnauthorizedTicketCommentException.class);

            verify(ticketCommentRepository, never()).save(any(TicketComment.class));
        }

        /*
        바로 아래 에러 사항 해결 부탁
        */
        @Test
        @DisplayName("담당자는 모든 티켓에 댓글을 작성할 수 있다")
        void should_CreateTicketComment_When_Manager() {
            // given
            Long ticketId = 1L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 테스트");

            Ticket ticket = mock(Ticket.class);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticket.canComment(any(User.class))).thenReturn(true);

            User mockManager = mock(User.class);
            when(mockManager.getUsername()).thenReturn("manager_user");
            when(ticket.getManager()).thenReturn(mockManager);

            User mockRequester = mock(User.class);
            when(mockRequester.getUsername()).thenReturn("requester_user");
            when(ticket.getRequester()).thenReturn(mockRequester);

            // 기존의 userDetailsManager를 스파이로 감싼다.
            CustomUserDetails spyUserDetailsManager = spy(userDetailsManager);
            User mockAuthor = mock(User.class);
            when(mockAuthor.getRole()).thenReturn(Role.MANAGER);
            when(spyUserDetailsManager.getUser()).thenReturn(mockAuthor);

            // when
            ticketCommentService.createTicketComment(ticketId, request, null, spyUserDetailsManager);

            // then: save() 대신 saveAndFlush() 검증
            verify(ticketCommentRepository, times(1)).saveAndFlush(any(TicketComment.class));
        }


    }

    @Nested
    @DisplayName("티켓 댓글 조회 테스트")
    class DescribeGetTicketComments {

        @Test
        @DisplayName("정상적으로 댓글을 조회하면 댓글 목록이 반환된다.")
        void should_GetTicketComments_When_ValidInput() {
            // given
            Long ticketId = 1L;

            Ticket mockTicket = mock(Ticket.class);
            when(ticketRepository.findById(ticketId))
                    .thenReturn(Optional.of(mockTicket));
            when(mockTicket.canComment(any(User.class))).thenReturn(true);

            List<TicketCommentResponse> mockList = List.of(
                    new TicketCommentResponse(1L, 2L, "작성자", "댓글 테스트", null, null),
                    new TicketCommentResponse(2L, 2L, "작성자", "댓글 테스트", null, null)
            );
            when(ticketCommentRepository.getTicketComments(ticketId))
                    .thenReturn(mockList);

            // when
            List<TicketCommentResponse> result = ticketCommentService.getTicketComments(ticketId, userDetailsUser);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContent()).isEqualTo("댓글 테스트");
        }

        @Test
        @DisplayName("요청한 티켓이 존재하지 않으면 예외가 발생한다.")
        void should_ThrowException_When_TicketNotFound() {
            // given
            Long ticketId = 1L;

            when(ticketRepository.findById(ticketId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketCommentService.getTicketComments(ticketId, userDetailsUser))
                    .isInstanceOf(TicketNotFoundException.class);
        }

        @Test
        @DisplayName("요청한 티켓에 댓글을 조회할 권한이 없으면 예외가 발생한다.")
        void should_ThrowException_When_UnauthorizedTicketComment() {
            // given
            Long ticketId = 1L;

            Ticket mockTicket = mock(Ticket.class);
            when(ticketRepository.findById(ticketId))
                    .thenReturn(Optional.of(mockTicket));
            when(mockTicket.canComment(any(User.class))).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> ticketCommentService.getTicketComments(ticketId, userDetailsUser))
                    .isInstanceOf(UnauthorizedTicketCommentException.class);
        }
    }

    @Nested
    @DisplayName("티켓 댓글 수정 테스트")
    class DescribeUpdateTicketComment {

        @Test
        @DisplayName("정상적으로 댓글을 수정하면 댓글이 수정된다.")
        void should_UpdateTicketComment_When_ValidInput() {
            // given
            Long ticketId = 1L;
            Long commentId = 1L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 수정 테스트");

            TicketComment mockComment = mock(TicketComment.class);
            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.of(mockComment));
            doNothing().when(mockComment).validateTicket(ticketId);
            doNothing().when(mockComment).validateAuthor(any(User.class));

            // when
            ticketCommentService.updateTicketComment(ticketId, commentId, request, userDetailsUser);

            // then
            verify(mockComment, times(1)).updateComment(request.getContent());
        }

        @Test
        @DisplayName("요청한 댓글이 존재하지 않으면 예외가 발생한다.")
        void should_ThrowException_When_TicketCommentNotFound() {
            // given
            Long ticketId = 1L;
            Long commentId = 999L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 수정 테스트");

            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketCommentService.updateTicketComment(ticketId, commentId, request, userDetailsUser))
                    .isInstanceOf(TicketCommentNotFoundException.class);
        }

        @Test
        @DisplayName("요청한 댓글의 티켓이 일치하지 않으면 예외가 발생한다.")
        void should_ThrowException_When_InvalidTicket() {
            // given
            Long ticketId = 999L;
            Long commentId = 1L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 수정 테스트");

            TicketComment mockComment = mock(TicketComment.class);
            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.of(mockComment));
            doThrow(UnauthorizedTicketCommentException.class)
                    .when(mockComment).validateTicket(ticketId);

            // when & then
            assertThatThrownBy(() -> ticketCommentService.updateTicketComment(ticketId, commentId, request, userDetailsUser))
                    .isInstanceOf(UnauthorizedTicketCommentException.class);
        }

        @Test
        @DisplayName("요청한 댓글의 작성자가 아니면 예외가 발생한다.")
        void should_ThrowException_When_UnauthorizedAuthor() {
            // given
            Long ticketId = 1L;
            Long commentId = 1L;
            TicketCommentRequest request = new TicketCommentRequest("댓글 수정 테스트");

            TicketComment mockComment = mock(TicketComment.class);
            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.of(mockComment));
            doThrow(UnauthorizedTicketCommentException.class)
                    .when(mockComment).validateAuthor(any(User.class));

            // when & then
            assertThatThrownBy(() -> ticketCommentService.updateTicketComment(ticketId, commentId, request, userDetailsUser))
                    .isInstanceOf(UnauthorizedTicketCommentException.class);
        }
    }

    @Nested
    @DisplayName("티켓 댓글 삭제 테스트")
    class DescribeDeleteTicketComment {

        @Test
        @DisplayName("정상적으로 댓글을 삭제하면 댓글이 삭제된다.")
        void should_DeleteTicketComment_When_ValidInput() {
            // given
            Long ticketId = 1L;
            Long commentId = 1L;

            TicketComment mockComment = mock(TicketComment.class);
            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.of(mockComment));
            doNothing().when(mockComment).validateTicket(ticketId);
            doNothing().when(mockComment).validateAuthor(any(User.class));

            // when
            ticketCommentService.deleteTicketComment(ticketId, commentId, userDetailsUser);

            // then
            verify(ticketCommentRepository, times(1)).delete(mockComment);
        }

        @Test
        @DisplayName("요청한 댓글이 존재하지 않으면 예외가 발생한다.")
        void should_ThrowException_When_TicketCommentNotFound() {
            // given
            Long ticketId = 1L;
            Long commentId = 999L;

            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ticketCommentService.deleteTicketComment(ticketId, commentId, userDetailsUser))
                    .isInstanceOf(TicketCommentNotFoundException.class);
        }

        @Test
        @DisplayName("요청한 댓글의 티켓이 일치하지 않으면 예외가 발생한다.")
        void should_ThrowException_When_InvalidTicket() {
            // given
            Long ticketId = 999L;
            Long commentId = 1L;

            TicketComment mockComment = mock(TicketComment.class);
            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.of(mockComment));
            doThrow(UnauthorizedTicketCommentException.class)
                    .when(mockComment).validateTicket(ticketId);

            // when & then
            assertThatThrownBy(() -> ticketCommentService.deleteTicketComment(ticketId, commentId, userDetailsUser))
                    .isInstanceOf(UnauthorizedTicketCommentException.class);
        }

        @Test
        @DisplayName("요청한 댓글의 작성자가 아니면 예외가 발생한다.")
        void should_ThrowException_When_UnauthorizedAuthor() {
            // given
            Long ticketId = 1L;
            Long commentId = 1L;

            TicketComment mockComment = mock(TicketComment.class);
            when(ticketCommentRepository.findById(commentId))
                    .thenReturn(Optional.of(mockComment));
            doThrow(UnauthorizedTicketCommentException.class)
                    .when(mockComment).validateAuthor(any(User.class));

            // when & then
            assertThatThrownBy(() -> ticketCommentService.deleteTicketComment(ticketId, commentId, userDetailsUser))
                    .isInstanceOf(UnauthorizedTicketCommentException.class);
        }
    }
}

