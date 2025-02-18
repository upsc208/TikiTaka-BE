package com.trillion.tikitaka.tickettemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class TicketTemplateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private TicketTemplateRepository templateRepository;

    private String validToken;
    private User testUser;
    private Category firstCat;
    private Category secondCat;
    private TicketType type1;
    private TicketTemplate template1;
    private TicketTemplate template2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        templateRepository.deleteAll();

        testUser = User.builder()
                .username("testUser")
                .email("test@domain.com")
                .password("pass")
                .role(Role.MANAGER)
                .build();
        testUser = userRepository.save(testUser);

        firstCat = Category.builder()
                .name("First Category")
                .build();
        firstCat = categoryRepository.save(firstCat);

        secondCat = Category.builder()
                .name("Second Category")
                .parent(firstCat)
                .build();
        secondCat = categoryRepository.save(secondCat);

        type1 = TicketType.builder()
                .name("Bug")
                .build();
        type1 = ticketTypeRepository.save(type1);

        template1 = TicketTemplate.builder()
                .templateTitle("Init Title")
                .title("Init T1")
                .description("Init Description")
                .requester(testUser)
                .firstCategory(firstCat)
                .secondCategory(secondCat)
                .type(type1)
                .manager(null)
                .build();
        template1 = templateRepository.save(template1);

        User otherUser = User.builder()
                .username("otherUser")
                .email("other@domain.com")
                .password("pass")
                .role(Role.MANAGER)
                .build();
        otherUser = userRepository.save(otherUser);

        template2 = TicketTemplate.builder()
                .templateTitle("Other Title")
                .title("Other T2")
                .description("Other Description")
                .requester(otherUser)
                .firstCategory(firstCat)
                .secondCategory(secondCat)
                .type(type1)
                .manager(null)
                .build();
        template2 = templateRepository.save(template2);

        validToken = jwtUtil.createJwtToken(
                JwtUtil.TOKEN_TYPE_ACCESS,
                testUser.getId(),
                testUser.getUsername(),
                testUser.getRole().toString(),
                JwtUtil.ACCESS_TOKEN_EXPIRATION
        );
    }

    @Test
    @DisplayName("[생성] 정상적인 방법으로 티켓 생성 요청")
    void createTemplate_Success() throws Exception {

        TicketTemplateRequest request = new TicketTemplateRequest(
                type1.getId(),
                firstCat.getId(),
                secondCat.getId(),
                101L,
                "Sample Template Title",
                "Sample Title",
                "Sample Description"
        );

        mockMvc.perform(post("/ticket/templates")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").exists())
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다"));
    }

    @Test
    @DisplayName("[생성] 2차 카테고리가 1차 카테고리에 속하지 않고 생성 요청")
    void createTemplate_CategoryMismatch() throws Exception {

        TicketTemplateRequest request = new TicketTemplateRequest(
                type1.getId(),
                firstCat.getId(),
                999L,
                101L,
                "Sample Template Title",
                "Sample Title",
                "Sample Description"
        );

        mockMvc.perform(post("/ticket/templates")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("2차 카테고리가 1차 카테고리에 속하지 않습니다."));
    }

    @Test
    @DisplayName("[수정] 정상적인 방법으로 수정 요청")
    void updateTemplate_Success() throws Exception {
        TicketTemplateRequest request = new TicketTemplateRequest(
                type1.getId(),
                firstCat.getId(),
                secondCat.getId(),
                101L,
                "Updated Template Title",
                "Updated Title",
                "Updated Description"
        );

        mockMvc.perform(patch("/ticket/templates/{templateId}", template1.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").value(template1.getId()))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다"));
    }

    @Test
    @DisplayName("[수정] 2차 카테고리가 1차에 속하지 않으면서 수정 요청")
    void updateTemplate_CategoryMismatch() throws Exception {
        TicketTemplateRequest request = new TicketTemplateRequest(
                type1.getId(),
                1L,
                999L,
                101L,
                "Mismatch Title",
                "Mismatch Title",
                "Mismatch Desc"
        );

        mockMvc.perform(patch("/ticket/templates/{templateId}", template1.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("2차 카테고리가 1차 카테고리에 속하지 않습니다."));
    }

    @Test
    @DisplayName("[삭제] 권한 없는 유저가 삭제")
    void deleteTemplate_Forbidden() throws Exception {
        mockMvc.perform(delete("/ticket/templates/{templateId}", template2.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("[조회] 존재하지 않는 템플릿 삭제")
    void getTemplate_NotFound() throws Exception {
        mockMvc.perform(get("/ticket/templates/{templateId}", 99999)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("티켓 템플릿을 찾을 수 없습니다."));
    }
}
