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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("🎟 티켓 폼 통합 테스트")
public class TicketFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketFormRepository ticketFormRepository;


    private CategoryRepository categoryRepository;

    private final Long FIRST_CATEGORY_ID = 1L;
    private final Long INVALID_FIRST_CATEGORY_ID = 3L;
    private final Long SECOND_CATEGORY_ID = 2L;
    private final Long INVALID_SECOND_CATEGORY_ID = 4L;

    @BeforeEach
    void setUp() {
        //ticketFormRepository.deleteAll();
    }


    @Test
    @DisplayName("티켓 폼 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateTicketForm_When_ValidRequest() throws Exception {

        TicketFormRequest request = new TicketFormRequest("필수 설명입니다.", "일반 설명입니다.");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", 3L, 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 티켓 폼 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_InvalidCategoryId() throws Exception {
        TicketFormRequest request = new TicketFormRequest("필수 설명입니다.", "일반 설명입니다.");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", INVALID_FIRST_CATEGORY_ID, SECOND_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }


    @Test
    @DisplayName("2차 카테고리가 1차 카테고리의 하위가 아닌경우 일때 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_SecondCategoryNotChildOfFirstCategory() throws Exception {
        TicketFormRequest request = new TicketFormRequest("필수 설명입니다.", "일반 설명입니다.");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", FIRST_CATEGORY_ID, INVALID_SECOND_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("티켓 폼 조회")
    @WithMockUser(username = "user", authorities = {"USER"})
    void should_GetTicketForm_When_ValidRequest() throws Exception {
        mockMvc.perform(get("/tickets/forms/{firstCategoryId}/{secondCategoryId}", FIRST_CATEGORY_ID, SECOND_CATEGORY_ID))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("존재하지 않는 티켓 폼 조회")
    @WithMockUser(username = "user", authorities = {"USER"})
    void should_ReturnNull_When_TicketFormNotFound() throws Exception {
        mockMvc.perform(get("/tickets/forms/{firstCategoryId}/{secondCategoryId}", 100L, 200L))
                .andExpect(status().isNotFound());

    }


    @Test
    @DisplayName("티켓 폼 수정")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_UpdateTicketForm_When_ValidRequest() throws Exception {

        TicketFormRequest request = new TicketFormRequest("수정된 필수 설명", "수정된 설명");

        mockMvc.perform(patch("/tickets/forms/{firstCategoryId}/{secondCategoryId}", FIRST_CATEGORY_ID, SECOND_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("티켓 폼 삭제")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_DeleteTicketForm_When_ValidRequest() throws Exception {

        mockMvc.perform(delete("/tickets/forms/{firstCategoryId}/{secondCategoryId}", FIRST_CATEGORY_ID, SECOND_CATEGORY_ID))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("존재하지 않는 티켓 폼 삭제")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_DeleteNonExistentTicketForm() throws Exception {
        mockMvc.perform(delete("/tickets/forms/{firstCategoryId}/{secondCategoryId}", 100L, 200L))
                .andExpect(status().isNotFound());
    }
}
