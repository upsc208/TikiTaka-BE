package com.trillion.tikitaka.subtask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("하위 태스크 (Subtask) 통합 테스트")
public class SubtaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Ticket ticket;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testUser", "MANAGER");
        ticket = Ticket.builder()
                .title("테스트 티켓")
                .description("테스트 설명")
                .status(Ticket.Status.PENDING)
                .requester(user)
                .build();
    }

    @Test
    @DisplayName("하위 태스크를 생성하면 200을 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateSubtask_when_ValidRequest() throws Exception {
        // given
        SubtaskRequest request = new SubtaskRequest(1L, "새로운 하위태스크");
        String jsonRequest = mapper.writeValueAsString(request);

        // when
        String responseBody = mockMvc.perform(post("/subtasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        assertThat(jsonNode.get("message").asText()).isEqualTo("태스크가 생성되었습니다");
    }

    @Test
    @DisplayName("존재하지 않는 티켓 ID로 하위 태스크 생성 시 404 Not Found 반환")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_Return404_when_TicketNotFound() throws Exception {
        // given
        SubtaskRequest request = new SubtaskRequest(999L, "없는 티켓의 하위 태스크");
        String jsonRequest = mapper.writeValueAsString(request);

        // when
        String responseBody = mockMvc.perform(post("/subtasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("해당 티켓을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("하위 태스크 목록을 조회하면 200 OK와 리스트를 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ReturnSubtasks_when_ValidTicketId() throws Exception {

        // when
        String responseBody = mockMvc.perform(get("/subtasks/" + 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        List<SubtaskResponse> responseList = mapper.readValue(jsonNode.get("data").toString(),
                mapper.getTypeFactory().constructCollectionType(List.class, SubtaskResponse.class));

        // then
        assertThat(responseList).hasSize(5);
    }

    @Test
    @DisplayName("하위 태스크 삭제하면 204 No Content 반환")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_DeleteSubtask_when_ValidTaskId() throws Exception {

        // when
        mockMvc.perform(delete("/subtasks/" + 1L + "/" + 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 하위 태스크 삭제 시 404 Not Found 반환")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_Return404_when_DeletingNonExistingSubtask() throws Exception {
        // when
        String responseBody = mockMvc.perform(delete("/subtasks/" + 1L + "/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("해당 하위태스크를 찾을수없습니다");
    }

}
