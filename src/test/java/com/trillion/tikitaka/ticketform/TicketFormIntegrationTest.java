package com.trillion.tikitaka.ticketform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import com.trillion.tikitaka.ticketform.dto.request.TicketFormRequest;
import com.trillion.tikitaka.ticketform.infrastructure.TicketFormRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("🎟티켓 폼 통합 테스트")
public class TicketFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketFormRepository ticketFormRepository;

    private CategoryRepository categoryRepository;

    private Long firstCategoryId;
    private Long secondCategoryId;

    private Long invalidFirstCategoryId;
    private Long invalidSecondCategoryId;

    @BeforeEach
    void setUp() {
        ticketFormRepository.deleteAll();
        categoryRepository.deleteAll();

        Category firstCategory = categoryRepository.save(new Category("1차 카테고리", null));
        Category secondCategory = categoryRepository.save(new Category("2차 카테고리", firstCategory));

        Category invalidFirstCategory = categoryRepository.save(new Category("잘못된 1차 카테고리", null));
        Category invalidSecondCategory = categoryRepository.save(new Category("잘못된 2차 카테고리", null));

        firstCategoryId = firstCategory.getId();
        secondCategoryId = secondCategory.getId();
        invalidFirstCategoryId = invalidFirstCategory.getId();
        invalidSecondCategoryId = invalidSecondCategory.getId();

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        ticketFormRepository.save(new TicketForm(firstCategory, secondCategory, "기본 설명", "기본 필수 설명"));
    }

    @Test
    @DisplayName("티켓 폼 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateTicketForm_When_ValidRequest() throws Exception {
        TicketFormRequest request = new TicketFormRequest("새로운 필수 설명", "새로운 일반 설명");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", firstCategoryId, secondCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(ticketFormRepository.findById(new TicketFormId(firstCategoryId, secondCategoryId))).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 티켓 폼 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_InvalidCategoryId() throws Exception {
        TicketFormRequest request = new TicketFormRequest("필수 설명입니다.", "일반 설명입니다.");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", invalidFirstCategoryId, secondCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("2차 카테고리가 1차 카테고리의 하위가 아닌 경우 생성 실패")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_SecondCategoryNotChildOfFirstCategory() throws Exception {
        TicketFormRequest request = new TicketFormRequest("필수 설명입니다.", "일반 설명입니다.");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", firstCategoryId, invalidSecondCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("티켓 폼 조회")
    @WithMockUser(username = "user", authorities = {"USER"})
    void should_GetTicketForm_When_ValidRequest() throws Exception {
        mockMvc.perform(get("/tickets/forms/{firstCategoryId}/{secondCategoryId}", firstCategoryId, secondCategoryId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 티켓 폼 조회")
    @WithMockUser(username = "user", authorities = {"USER"})
    void should_ReturnNotFound_When_TicketFormNotFound() throws Exception {
        mockMvc.perform(get("/tickets/forms/{firstCategoryId}/{secondCategoryId}", invalidFirstCategoryId, invalidSecondCategoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("티켓 폼 수정")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_UpdateTicketForm_When_ValidRequest() throws Exception {
        TicketFormRequest request = new TicketFormRequest("수정된 필수 설명", "수정된 설명");

        mockMvc.perform(patch("/tickets/forms/{firstCategoryId}/{secondCategoryId}", firstCategoryId, secondCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        TicketForm updatedTicketForm = ticketFormRepository.findById(new TicketFormId(firstCategoryId, secondCategoryId)).orElseThrow();
        assertThat(updatedTicketForm.getMustDescription()).isEqualTo("수정된 필수 설명");
        assertThat(updatedTicketForm.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("2차 카테고리가 1차 카테고리의 하위가 아닌 경우 티켓 폼 수정 실패")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_SecondCategoryNotChildOfFirstCategory_OnUpdate() throws Exception {
        TicketFormRequest request = new TicketFormRequest("수정된 필수 설명", "수정된 설명");

        mockMvc.perform(patch("/tickets/forms/{firstCategoryId}/{secondCategoryId}", firstCategoryId, invalidSecondCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("티켓 폼 삭제")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_DeleteTicketForm_When_ValidRequest() throws Exception {
        mockMvc.perform(delete("/tickets/forms/{firstCategoryId}/{secondCategoryId}", firstCategoryId, secondCategoryId))
                .andExpect(status().isOk());

        assertThat(ticketFormRepository.findById(new TicketFormId(firstCategoryId, secondCategoryId))).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 티켓 폼 삭제 실패")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_DeleteNonExistentTicketForm() throws Exception {
        mockMvc.perform(delete("/tickets/forms/{firstCategoryId}/{secondCategoryId}", invalidFirstCategoryId, invalidSecondCategoryId))
                .andExpect(status().isNotFound());
    }
}
