package com.trillion.tikitaka.tickettype.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.exception.GlobalExceptionHandler;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.tickettype.dto.request.TicketTypeRequest;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("티켓 타입 통합 테스트")
public class TicketTypeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;


    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("정상적인 티켓 타입 생성")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_CreateTicketType_When_ValidRequest() throws Exception {
        // given
        TicketTypeRequest request = new TicketTypeRequest("Bug");

        // when & then
        mockMvc.perform(post("/tickets/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(ticketTypeRepository.findByName("Bug")).isPresent();
    }

    @Test
    @DisplayName("중복된 티켓 타입 생성 시 예외 발생")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_DuplicateTicketType() throws Exception {
        // given
        ticketTypeRepository.save(new com.trillion.tikitaka.tickettype.domain.TicketType("Bug"));
        TicketTypeRequest request = new TicketTypeRequest("Bug");

        // when & then
        String responseBody = mockMvc.perform(post("/tickets/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("정상적인 티켓 타입 수정")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_UpdateTicketType_When_ValidRequest() throws Exception {
        // given
        var ticketType = ticketTypeRepository.save(new com.trillion.tikitaka.tickettype.domain.TicketType("Bug"));
        TicketTypeRequest request = new TicketTypeRequest("Feature");

        // when & then
        mockMvc.perform(patch("/tickets/types/" + ticketType.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(ticketTypeRepository.findById(ticketType.getId()).get().getName()).isEqualTo("Feature");
    }

    @Test
    @DisplayName("존재하지 않는 타입 ID 수정 시 예외 발생")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_TypeIdNotFound() throws Exception {
        // given
        TicketTypeRequest request = new TicketTypeRequest("Feature");

        // when & then
        mockMvc.perform(patch("/tickets/types/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("중복된 티켓 타입명 수정 시 예외 발생")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_DuplicateTypeNameOnUpdate() throws Exception {
        // given
        ticketTypeRepository.save(new com.trillion.tikitaka.tickettype.domain.TicketType("Bug"));
        var ticketType = ticketTypeRepository.save(new com.trillion.tikitaka.tickettype.domain.TicketType("Feature"));
        TicketTypeRequest request = new TicketTypeRequest("Bug");

        // when & then
        mockMvc.perform(patch("/tickets/types/" + ticketType.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    @Test
    @DisplayName("기본 티켓 타입 수정 시 예외 발생-포스트맨에서는 예외 발생 확인 테스트 코드에서는 성공 처리로 케이스 통과 실패")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_UpdatingDefaultTicketType() throws Exception {
        // given
        TicketTypeRequest request = new TicketTypeRequest("New Name");

        // when & then
        mockMvc.perform(patch("/tickets/types/" + 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("정상적인 티켓 타입 삭제")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_DeleteTicketType_When_ValidId() throws Exception {
        // given
        var ticketType = ticketTypeRepository.save(new com.trillion.tikitaka.tickettype.domain.TicketType("Bug"));

        // when & then
        mockMvc.perform(delete("/tickets/types/" + ticketType.getId()))
                .andExpect(status().isOk());

        assertThat(ticketTypeRepository.findById(ticketType.getId())).isNotPresent();
    }

    @Test
    @DisplayName("존재하지 않는 티켓 타입 삭제 시 예외 발생")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_DeleteNonExistentType() throws Exception {
        // when & then
        mockMvc.perform(delete("/tickets/types/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("기본 티켓 타입 삭제 시 예외 발생")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ThrowException_When_DeletingDefaultTicketType() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/tickets/types/" + 3L))
                .andExpect(status().isInternalServerError());
    }

}
