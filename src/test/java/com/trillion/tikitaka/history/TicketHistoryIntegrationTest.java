package com.trillion.tikitaka.history;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("히스토리 조회 통합 테스트")
public class TicketHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HistoryRepository historyRepository;

    private final ObjectMapper mapper = new ObjectMapper();

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
    private Category parentCategory2;
    private Category childCategory2;



    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
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

        userDetails = new CustomUserDetails(normalUser1);

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

        Ticket ticket2 = ticketRepository.saveAndFlush(Ticket.builder()
                .title("TicketB")
                .description("Desc B")
                .ticketType(ticketType2)
                .firstCategory(parentCategory1)
                .secondCategory(childCategory1)
                .requester(normalUser1)
                .manager(manager1)
                .deadline(LocalDateTime.parse("2025-06-10 15:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .status(Ticket.Status.PENDING)
                .build());

        ticketRepository.flush();
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket1)
                .updateType(TicketHistory.UpdateType.STATUS_CHANGE)
                .updatedBy(admin1)
                .updatedAt(LocalDateTime.now())
                .build();

        historyRepository.saveAndFlush(history);
    }




    @Test
    @DisplayName("비인증 사용자가 이력 조회를 시도하면 401 Unauthorized를 반환한다.")
    void should_Return401_when_UnauthenticatedUserRequestsHistory() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "1")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("인증되지 않은 사용자입니다.");
    }

    @Test
    @DisplayName("권한이 부족한 사용자가 이력 조회를 시도하면 403 Forbidden을 반환한다.")
    @WithMockUser(username = "user", roles = {"USER"})
    void should_Return403_when_UserWithoutPermissionRequestsHistory() throws Exception {
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "1")
                        .contentType("application/json"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        // then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("관리자가 정상적으로 이력을 조회하면 200 OK와 데이터를 반환한다.")
    @WithMockUser(username = "admin1", authorities = {"ADMIN"})
    void should_Return200_when_AdminRequestsHistory() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();

        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", String.valueOf(ticketId))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        JsonNode contentNode = jsonNode.get("data").get("content");

        List<HistoryResponse> historyList = new ArrayList<>();
        for (JsonNode node : contentNode) {
            HistoryResponse response = mapper.treeToValue(node, HistoryResponse.class);
            historyList.add(response);
        }

        // then
        assertThat(historyList).isNotEmpty();
    }


    @Test
    @DisplayName("존재하지 않는 티켓 ID로 이력 조회 시 404 Not Found를 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_Return404_when_TicketNotFound() throws Exception {
        Long invalidTicketId = ticketRepository.findAll().stream()
                .map(Ticket::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1000;

        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", String.valueOf(invalidTicketId))
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("해당 티켓을 찾을 수 없습니다.");
    }


    @Test
    @DisplayName("이력이 없는 경우 200 OK와 빈 배열을 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ReturnEmptyList_when_NoHistoryExists() throws Exception {
        // ✅ 히스토리가 없는 티켓 찾기
        Ticket ticketWithoutHistory = ticketRepository.findAll().stream()
                .filter(ticket -> historyRepository.findByTicketId(ticket.getId()).isEmpty())  // ✅ 올바른 필터 조건
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("히스토리가 없는 티켓이 없습니다."));

        Long ticketId = ticketWithoutHistory.getId();

        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", String.valueOf(ticketId))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        JsonNode contentNode = jsonNode.get("data").get("content");

        // then
        assertThat(contentNode).isEmpty();
    }



    @Test
    @DisplayName("관리자가 티켓 상태를 변경하면 변경 이력이 기록된다.")
    @WithUserDetails(value = "admin.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_RecordHistory_when_AdminUpdatesTicketStatus() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();

        // given
        EditSettingRequest request = new EditSettingRequest(null, null, null, null, Ticket.Status.IN_PROGRESS, null);
        String jsonRequest = mapper.writeValueAsString(request);

        // when
        mockMvc.perform(patch("/tickets/" + ticketId + "/status") // ✅ 변경된 ID 사용
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // then
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", String.valueOf(ticketId))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        JsonNode contentNode = jsonNode.get("data").get("content");

        List<HistoryResponse> historyList = new ArrayList<>();
        for (JsonNode node : contentNode) {
            HistoryResponse response = mapper.treeToValue(node, HistoryResponse.class);
            historyList.add(response);
        }

        assertThat(historyList).isNotEmpty();
        assertThat(historyList.get(0).getUpdateType()).isEqualTo(TicketHistory.UpdateType.STATUS_CHANGE);
    }



    @Test
    @DisplayName("일반 사용자가 티켓 상태를 변경하려 하면 403 Forbidden을 반환하고 이력이 기록되지 않는다.")
    @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_Return403AndNotRecordHistory_when_UserWithoutPermissionUpdatesTicket() throws Exception {

        Ticket ticketWithoutHistory = ticketRepository.findAll().stream()
                .filter(ticket -> historyRepository.findByTicketId(ticket.getId()).isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("히스토리가 없는 티켓이 없습니다."));
        Long ticketId = ticketWithoutHistory.getId();

        // given
        EditSettingRequest request = new EditSettingRequest(null, null, null, null, Ticket.Status.IN_PROGRESS, null);
        String jsonRequest = mapper.writeValueAsString(request);

        // when (권한 없는 사용자가 상태 변경 요청)
        String responseBody = mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isForbidden()) // ✅ 403 Forbidden 기대
                .andReturn().getResponse().getContentAsString();

        // then (에러 메시지 검증)
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("접근 권한이 없습니다.");

        String historyResponse = mockMvc.perform(get("/history")
                        .param("ticketId", String.valueOf(ticketId)) // ✅ 동적으로 ID 사용
                        .contentType("application/json"))
                .andExpect(status().isOk()) // ✅ 200 OK 기대
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(historyResponse);
        JsonNode contentNode = jsonNode.get("data").get("content");

        List<HistoryResponse> historyList = new ArrayList<>();
        for (JsonNode node : contentNode) {
            HistoryResponse response = mapper.treeToValue(node, HistoryResponse.class);
            historyList.add(response);
        }

        // ✅ 변경 이력이 없어야 함
        assertThat(historyList).isEmpty();
    }

}
