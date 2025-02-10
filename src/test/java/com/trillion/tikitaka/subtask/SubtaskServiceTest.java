package com.trillion.tikitaka.subtask;

import com.trillion.tikitaka.subtask.application.SubtaskService;
import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.exception.SubtaskNotFoundExeption;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Subtask 유닛 테스트")
class SubtaskServiceTest {

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private TicketRepository ticketRepository;

    private SubtaskService subtaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subtaskService = new SubtaskService(subtaskRepository, ticketRepository);
    }

    @Nested
    @DisplayName("하위 태스크 생성 테스트")
    class DescribeCreateSubtask {
        @Test
        @DisplayName("유효한 요청이면 하위 태스크를 생성한다.")
        void should_CreateSubtask_when_ValidRequest() {
            // given
            Long ticketId = 1L;
            SubtaskRequest request = new SubtaskRequest(ticketId,"새로운 하위태스크");
            Ticket ticket = mock(Ticket.class);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            // when
            subtaskService.createSubtask(request);

            // then
            verify(subtaskRepository, times(1)).save(any(Subtask.class));
        }

        @Test
        @DisplayName("존재하지 않는 티켓 ID로 하위 태스크 생성 시 예외 발생")
        void should_ThrowException_when_TicketNotFound() {
            // given
            Long ticketId = 999L;
            SubtaskRequest request = new SubtaskRequest(ticketId, "새로운 하위 태스크");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subtaskService.createSubtask(request))
                    .isInstanceOf(TicketNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("하위 태스크 조회 테스트")
    class DescribeGetSubtasks {
        @Test
        @DisplayName("존재하는 티켓 ID로 하위 태스크를 조회한다.")
        void should_ReturnSubtasks_when_TicketExists() {
            // given
            Long ticketId = 1L;
            Ticket ticket = mock(Ticket.class);
            List<Subtask> subtasks = List.of(new Subtask(1L, "설명", ticket, false));

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(subtaskRepository.findByParentTicket(ticket)).thenReturn(subtasks);

            // when
            List<SubtaskResponse> responses = subtaskService.getSubtasksByTicketId(ticketId);

            // then
            assertThat(responses).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 티켓 ID로 하위 태스크 조회 시 예외 발생")
        void should_ThrowException_when_TicketNotFound() {
            // given
            Long ticketId = 999L;
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subtaskService.getSubtasksByTicketId(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("하위 태스크 삭제 테스트")
    class DescribeDeleteSubtask {
        @Test
        @DisplayName("존재하는 하위 태스크를 삭제한다.")
        void should_DeleteSubtask_when_ValidTaskId() {
            // given
            Long taskId = 1L;
            Long ticketId = 1L;
            Subtask subtask = mock(Subtask.class);

            when(subtaskRepository.findById(taskId)).thenReturn(Optional.of(subtask));

            // when
            subtaskService.deleteSubtask(taskId, ticketId);

            // then
            verify(subtaskRepository, times(1)).delete(subtask);
        }

        @Test
        @DisplayName("존재하지 않는 하위 태스크 삭제 시 예외 발생")
        void should_ThrowException_when_SubtaskNotFound() {
            // given
            Long taskId = 999L;
            Long ticketId = 1L;
            when(subtaskRepository.findById(taskId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> subtaskService.deleteSubtask(taskId, ticketId))
                    .isInstanceOf(SubtaskNotFoundExeption.class);
        }
    }
}
