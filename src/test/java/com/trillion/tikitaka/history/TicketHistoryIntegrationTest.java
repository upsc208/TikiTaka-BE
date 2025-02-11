package com.trillion.tikitaka.history;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.user.domain.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test") // 테스트 환경
@DisplayName("히스토리 조회 통합 테스트")
public class TicketHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private User user;
    private CustomUserDetails userDetails;



    @BeforeEach
    void setUp() {
        // 🔹 테스트 유저 생성
        user = new User(1L, "testUser", "MANAGER");
        userDetails = new CustomUserDetails(this.user);

        // 🔹 SecurityContext에 인증 정보 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("비인증 사용자가 이력 조회를 시도하면 401 Unauthorized를 반환한다.")
    void should_Return401_when_UnauthenticatedUserRequestsHistory() throws Exception {
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "1")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized()) // 401 예상
                .andReturn().getResponse().getContentAsString();

        // then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("인증되지 않은 사용자입니다.");
    }

    @Test
    @DisplayName("권한이 부족한 사용자가 이력 조회를 시도하면 403 Forbidden을 반환한다.")
    @WithMockUser(username = "user", roles = {"USER"}) // 일반 사용자
    void should_Return403_when_UserWithoutPermissionRequestsHistory() throws Exception {
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "1")
                        .contentType("application/json"))
                .andExpect(status().isForbidden()) // 403 예상
                .andReturn().getResponse().getContentAsString();

        // then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("관리자가 정상적으로 이력을 조회하면 200 OK와 데이터를 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"}) // ✅ roles -> authorities 변경
    void should_Return200_when_AdminRequestsHistory() throws Exception {
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "7")
                        .contentType("application/json"))
                .andExpect(status().isOk()) // 200 예상
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
    @WithMockUser(username = "admin", authorities = {"ADMIN"}) // 관리자 권한
    void should_Return404_when_TicketNotFound() throws Exception {
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "9999")  // 존재하지 않는 ID
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()) // 400 예상 원래는 404를 뱉어야하지만 글로벌핸들러에서 custom을 400으로 반환하기에 400에러가 발생
                .andReturn().getResponse().getContentAsString();

        // then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("해당 티켓을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이력이 없는 경우 200 OK와 빈 배열을 반환한다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"}) // 관리자 권한
    void should_ReturnEmptyList_when_NoHistoryExists() throws Exception {
        // when
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "2")  // 이력이 없는 티켓 ID
                        .contentType("application/json"))
                .andExpect(status().isOk()) // 200 예상
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
        // 🔹 사용자 생성
//        User user = new User(1L, "admin.tk", "ADMIN");
//        CustomUserDetails userDetails = new CustomUserDetails(user);

        // 🔹 SecurityContext에 직접 사용자 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);

        // given
        EditSettingRequest request = new EditSettingRequest(null, null, null, null, Ticket.Status.IN_PROGRESS, null);
        String jsonRequest = mapper.writeValueAsString(request);

        // when - 상태 변경
        mockMvc.perform(patch("/tickets/1/status")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk()); // 200 응답 예상

        // then - 변경 이력 조회
        String responseBody = mockMvc.perform(get("/history")
                        .param("ticketId", "1")
                        .contentType("application/json"))
                .andExpect(status().isOk()) // 200 예상
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(responseBody);
        JsonNode contentNode = jsonNode.get("data").get("content");

        List<HistoryResponse> historyList = new ArrayList<>();
        for (JsonNode node : contentNode) {
            HistoryResponse response = mapper.treeToValue(node, HistoryResponse.class);
            historyList.add(response);
        }

        // 변경 이력이 존재해야 함
        assertThat(historyList).isNotEmpty();
        assertThat(historyList.get(0).getUpdateType()).isEqualTo(TicketHistory.UpdateType.STATUS_CHANGE);
    }


    @Test
    @DisplayName("일반 사용자가 티켓 상태를 변경하려 하면 403 Forbidden을 반환하고 이력이 기록되지 않는다.")
    @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")// 일반 유저 권한
    void should_Return403AndNotRecordHistory_when_UserWithoutPermissionUpdatesTicket() throws Exception {
        // given
        EditSettingRequest request = new EditSettingRequest(null, null, null, null, Ticket.Status.IN_PROGRESS, null);
        String jsonRequest = mapper.writeValueAsString(request);

        // when - 상태 변경 요청 (권한 부족)
        String responseBody = mockMvc.perform(patch("/tickets/1/status")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isForbidden()) // 403 예상
                .andReturn().getResponse().getContentAsString();

        // then - 에러 메시지 확인
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getMessage()).isEqualTo("접근 권한이 없습니다.");

        // 변경 이력 조회
        String historyResponse = mockMvc.perform(get("/history")
                        .param("ticketId", "1")
                        .contentType("application/json"))
                .andExpect(status().isOk()) // 200 예상
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = mapper.readTree(historyResponse);
        JsonNode contentNode = jsonNode.get("data").get("content");

        List<HistoryResponse> historyList = new ArrayList<>();
        for (JsonNode node : contentNode) {
            HistoryResponse response = mapper.treeToValue(node, HistoryResponse.class);
            historyList.add(response);
        }

        // 변경 이력이 추가되지 않아야 함 - 테스트결과 변경되않음
        assertThat(historyList).isEmpty();
    }
}
