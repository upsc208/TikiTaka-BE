package com.trillion.tikitaka.ticketform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticketcomment.infrastructure.TicketCommentRepository;
import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import com.trillion.tikitaka.ticketform.dto.request.TicketFormRequest;
import com.trillion.tikitaka.ticketform.infrastructure.TicketFormRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private TicketRepository ticketRepository;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

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


    private Long firstCategoryId;
    private Long secondCategoryId;

    private Long invalidFirstCategoryId;
    private Long invalidSecondCategoryId;

    @BeforeEach
    void setUp() {
        ticketFormRepository.deleteAll();
        categoryRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // ✅ 정상적인 카테고리 생성
        parentCategory1 = categoryRepository.saveAndFlush(new Category("카테고리A", null));
        childCategory1 = categoryRepository.saveAndFlush(new Category("카테고리A-1", parentCategory1));

        firstCategoryId = parentCategory1.getId();
        secondCategoryId = childCategory1.getId();

        // ✅ 잘못된 2차 카테고리 생성 (부모가 없는 상태 → 해결: 명확한 부모 설정)
        parentCategory2 = categoryRepository.saveAndFlush(new Category("카테고리B", null));
        childCategory2 = categoryRepository.saveAndFlush(new Category("카테고리B-1", parentCategory2)); // 🚀 부모 설정 완료

        // 🚀 flush() 호출
        categoryRepository.flush();
        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        ticketFormRepository.saveAndFlush(new TicketForm(parentCategory1, childCategory1, "기본 설명", "기본 필수 설명"));
        invalidFirstCategoryId = parentCategory2.getId();
        invalidSecondCategoryId = childCategory2.getId();

        // ✅ 디버깅 로그 추가
        System.out.println("✅ firstCategoryId: " + firstCategoryId);
        System.out.println("✅ secondCategoryId: " + secondCategoryId);
        System.out.println("✅ invalidFirstCategoryId: " + invalidFirstCategoryId);
        System.out.println("✅ invalidSecondCategoryId: " + invalidSecondCategoryId);

        assertThat(invalidSecondCategoryId).isNotNull(); // 🚨 여기서 실패하면 DB 저장 문제
    }



    @Test
    @DisplayName("티켓 폼 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateTicketForm_When_ValidRequest() throws Exception {
        // 새로운 카테고리 생성 (중복 방지)
        Category newParentCategory = categoryRepository.saveAndFlush(new Category("새로운 1차 카테고리", null));
        Category newChildCategory = categoryRepository.saveAndFlush(new Category("새로운 2차 카테고리", newParentCategory));

        Long newFirstCategoryId = newParentCategory.getId();
        Long newSecondCategoryId = newChildCategory.getId();

        // 새로운 티켓 폼 요청
        TicketFormRequest request = new TicketFormRequest("새로운 필수 설명", "새로운 일반 설명");

        mockMvc.perform(post("/tickets/forms/{firstCategoryId}/{secondCategoryId}", newFirstCategoryId, newSecondCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(ticketFormRepository.findById(new TicketFormId(newFirstCategoryId, newSecondCategoryId))).isPresent();
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

        assertThat(invalidSecondCategoryId).isNotNull();

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
