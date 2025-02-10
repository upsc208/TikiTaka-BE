package com.trillion.tikitaka.history;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @InjectMocks
    private HistoryService historyService;

    @Mock
    private HistoryRepository historyRepository;

    private Ticket ticket;
    private User user;
    private TicketHistory history;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testUser", "MANAGER");
        User requester = new User(2L,"testReq","USER");
        TicketType type = new TicketType("Name");
        Category category = new Category("test",null);
        LocalDateTime timeT = LocalDateTime.now();
        ticket = new Ticket(1L, "Test Ticket","content", Ticket.Priority.LOW, Ticket.Status.PENDING,type,category,null,timeT,requester,user,false,10.0 );
        history = TicketHistory.createHistory(ticket, user, TicketHistory.UpdateType.TICKET_EDITED);
    }

    @Test
    @DisplayName("티켓 이력 저장 테스트")
    void should_RecordTicketHistory_when_ticketEdited() {
        // Given
        when(historyRepository.save(any(TicketHistory.class))).thenReturn(history);

        // When
        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.TICKET_EDITED);

        // Then
        verify(historyRepository, times(1)).save(any(TicketHistory.class));
    }

    @Test
    @DisplayName("특정 티켓 이력 조회 테스트")
    void should_GetTicketHistory_when_ValidRequest() {
        // Given
        LocalDateTime timeT = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<HistoryResponse> historyResponses = List.of(
                new HistoryResponse(history.getId(), ticket.getId(), "title", "user", timeT, TicketHistory.UpdateType.TICKET_EDITED)
        );
        Page<HistoryResponse> historyPage = new PageImpl<>(historyResponses);

        when(historyRepository.getHistory(any(Pageable.class), anyLong(), anyLong(), anyString()))
                .thenReturn(historyPage);

        // When
        Page<HistoryResponse> result = historyService.getHistory(pageable, user.getId(), ticket.getId(), "TICKET_EDITED");

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUpdateType().toString()).isEqualTo("TICKET_EDITED");
    }

}
