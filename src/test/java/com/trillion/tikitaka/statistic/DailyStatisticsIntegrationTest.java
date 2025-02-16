package com.trillion.tikitaka.statistic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyCategoryStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyTypeStatisticsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("일간 통계 통합 테스트")
public class DailyStatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("✅ [요약] 금일 티켓 처리 현황 조회")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getDailySummaryTest() throws Exception {
        String responseBody = mockMvc.perform(get("/statistics/daily/summary")
                        .param("date", "2025-02-16"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<DailyStatisticsResponse> response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("요청이 성공적으로 처리되었습니다");
    }

    @Test
    @DisplayName("✅ [담당자] 금일 담당자별 티켓 통계 조회")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getDailyManagerSummaryTest() throws Exception {
        String responseBody = mockMvc.perform(get("/statistics/daily/manSummary")
                        .param("date", "2025-02-16"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("요청이 성공적으로 처리되었습니다");
    }

    @Test
    @DisplayName("✅ [유형] 금일 유형별 티켓 생성 수 조회")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getDailyTypeSummaryTest() throws Exception {
        String responseBody = mockMvc.perform(get("/statistics/daily/typeSummary")
                        .param("date", "2025-02-16"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<DailyTypeStatisticsResponse> response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("요청이 성공적으로 처리되었습니다");
    }

    @Test
    @DisplayName("✅ [카테고리] 금일 1차, 2차 카테고리별 티켓 생성 수 조회")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getDailyCategorySummaryTest() throws Exception {
        String responseBody = mockMvc.perform(get("/statistics/daily/catSummary")
                        .param("date", "2025-02-16"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<DailyCategoryStatisticsResponse> response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("요청이 성공적으로 처리되었습니다");
    }
}
