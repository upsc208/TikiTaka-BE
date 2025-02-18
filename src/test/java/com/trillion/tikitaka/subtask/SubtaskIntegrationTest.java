package com.trillion.tikitaka.subtask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubtaskRepository subtaskRepository;


    private User user;

    private User manager1;
    private User manager2;
    private User normalUser1;
    private User normalUser2;

    private User admin1;


    private CustomUserDetails userDetails;

    private TicketType ticketType1;
    private TicketType ticketType2;

    private Category parentCategory1;
    private Category childCategory1;

    @PersistenceContext
    private EntityManager entityManager;

    private Ticket ticket;
    private Category parentCategory2;
    private Category childCategory2;

    @BeforeEach
    void setUp() {
        subtaskRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        manager1 = userRepository.saveAndFlush(User.builder()
                .username("manager1")
                .email("manager1@test.com")
                .password("manager1pass")
                .role(Role.MANAGER)
                .build());

        normalUser1 = userRepository.saveAndFlush(User.builder()
                .username("normalUser1")
                .email("user1@test.com")
                .password("user1pass")
                .role(Role.USER)
                .build());

        admin1 = userRepository.saveAndFlush(User.builder()
                .username("admin1")
                .email("admin1@test.com")
                .password("admin1pass")
                .role(Role.ADMIN)
                .build());

        userRepository.flush();


        ticketType1 = ticketTypeRepository.saveAndFlush(new TicketType("기본 티켓 유형"));
        ticketType2 = ticketTypeRepository.saveAndFlush(new TicketType("두번째 티켓 유형"));


        parentCategory1 = categoryRepository.saveAndFlush(new Category("카테고리A", null));
        childCategory1 = categoryRepository.saveAndFlush(new Category("카테고리A-1", parentCategory1));


        Ticket ticket1 = ticketRepository.saveAndFlush(Ticket.builder()
                .title("TicketA")
                .description("Desc A")
                .ticketType(ticketType1)
                .firstCategory(parentCategory1)
                .secondCategory(childCategory1)
                .requester(normalUser1)
                .manager(manager1)
                .deadline(LocalDateTime.parse("2025-05-05 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .status(Ticket.Status.IN_PROGRESS)
                .build());


        ticketRepository.flush();


        Subtask subtask1 = Subtask.builder()
                .description("첫 번째 하위 태스크")
                .done(false)
                .parentTicket(ticket1)
                .build();

        Subtask subtask2 = Subtask.builder()
                .description("두 번째 하위 태스크")
                .done(false)
                .parentTicket(ticket1)
                .build();

        Subtask subtask3 = Subtask.builder()
                .description("세 번째 하위 태스크")
                .done(true)
                .parentTicket(ticket1)
                .build();


        subtaskRepository.saveAll(List.of(subtask1, subtask2, subtask3));
        subtaskRepository.flush();
    }


    @Test
    @DisplayName("하위 태스크를 생성하면 200을 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateSubtask_when_ValidRequest() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        // given
        SubtaskRequest request = new SubtaskRequest(ticketId, "새로운 하위태스크");
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
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("해당 티켓을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("하위 태스크 목록을 조회하면 200 OK와 리스트를 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ReturnSubtasks_when_ValidTicketId() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();



        // when
        String responseBody = mockMvc.perform(get("/subtasks/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        List<SubtaskResponse> responseList = mapper.readValue(jsonNode.get("data").toString(),
                mapper.getTypeFactory().constructCollectionType(List.class, SubtaskResponse.class));

        // then
        assertThat(responseList).hasSize(3);
    }

    @Test
    @DisplayName("하위 태스크 삭제하면 204 No Content 반환")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_DeleteSubtask_when_ValidTaskId() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        Subtask subtask  = subtaskRepository.findAll().stream().findFirst().orElseThrow();
        // when
        mockMvc.perform(delete("/subtasks/" + ticketId + "/" + subtask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 하위 태스크 삭제 시 404 Not Found 반환")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_Return404_when_DeletingNonExistingSubtask() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        // when
        String responseBody = mockMvc.perform(delete("/subtasks/" + ticketId + "/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("해당 하위태스크를 찾을수없습니다");
    }

}
