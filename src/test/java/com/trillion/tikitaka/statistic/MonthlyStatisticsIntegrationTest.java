package com.trillion.tikitaka.statistic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.dto.response.AllMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("월간 통계 통합 테스트")
public class MonthlyStatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("api를 통해 자동으로 월간 통계를 생성할 수 있다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateMonthlyStatistics_When_AdminUser() throws Exception {
        String responseBody = mockMvc.perform(post("/statistic/record")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<Void> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 월간 통계를 조회하면 401 UNAUTHORIZED 응답을 반환한다.")
    void should_Return401_When_UnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/statistic/monAll")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("MANAGER 권한을 가진 사용자는 월간 통계를 조회할 수 있다.")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_ReturnMonthlyStatistics_When_ManagerUser() throws Exception {
        String responseBody = mockMvc.perform(get("/statistic/monAll")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<AllMonth> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("잘못된 연도와 월을 입력하면 400 BAD REQUEST 응답을 반환한다.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void should_Return400_When_InvalidYearMonthProvided() throws Exception {
        mockMvc.perform(post("/statistic/record")
                        .param("year", "abcd")
                        .param("month", "99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN 사용자는 특정 타입별 통계를 조회할 수 있다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ReturnTypeStatistics_When_AdminUser() throws Exception {
        String responseBody = mockMvc.perform(get("/statistic/monType")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("MANAGER 사용자는 특정 사용자의 월간 통계를 조회할 수 있다.")
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    void should_ReturnUserStatistics_When_ManagerUser() throws Exception {
        String responseBody = mockMvc.perform(get("/statistic/monUser")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }
}
