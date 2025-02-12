package com.trillion.tikitaka.ticketcomment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticketcomment.dto.request.TicketCommentRequest;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticketcomment.infrastructure.TicketCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
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
@DisplayName("티켓 댓글 통합 테스트")
public class TicketCommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        //ticketCommentRepository.deleteAll();
       // ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 티켓 댓글 생성")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_CreateTicketComment_When_ValidRequest() throws Exception {
        // given
        //Ticket ticket = new Ticket(1L,"test","content",null,null,null,null,null,null,null,null,null,null);
        TicketCommentRequest request = new TicketCommentRequest("This is a test comment.");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",  // ✅ 컨트롤러의 @RequestPart 이름과 동일해야 함
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",  // ✅ 컨트롤러의 @RequestPart("files") 이름과 동일해야 함
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/tickets/" + 1L + "/comments")
                        .file(requestPart)  // ✅ JSON 데이터를 multipart로 전송
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }




    @Test
    @DisplayName("존재하지 않는 티켓에 댓글 생성 시 예외 발생")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_ThrowException_When_TicketNotFound() throws Exception {
        // given
        TicketCommentRequest request = new TicketCommentRequest("Invalid Ticket ID");

        // when & then
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",  // ✅ 컨트롤러의 @RequestPart 이름과 동일해야 함
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",  // ✅ 컨트롤러의 @RequestPart("files") 이름과 동일해야 함
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/tickets/" + 999L + "/comments")
                        .file(requestPart)  // ✅ JSON 데이터를 multipart로 전송
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("유효하지 않은 유저가 댓글을 생성할 경우 예외 발생")
    @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_ThrowException_When_InvalidUserCreatesComment() throws Exception {

        TicketCommentRequest request = new TicketCommentRequest("Unauthorized Comment Attempt");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",  // ✅ 컨트롤러의 @RequestPart 이름과 동일해야 함
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",  // ✅ 컨트롤러의 @RequestPart("files") 이름과 동일해야 함
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/tickets/" + 1L + "/comments")
                        .file(requestPart)  // ✅ JSON 데이터를 multipart로 전송
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("정상적인 티켓 댓글 조회")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_GetTicketComments_When_ValidRequest() throws Exception {

        mockMvc.perform(get("/tickets/" + 1L + "/comments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("티켓 댓글 수정 - 정상 수정")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_UpdateTicketComment_When_ValidRequest() throws Exception {
        // given
        TicketCommentRequest request = new TicketCommentRequest("Updated Comment");

        // when & then
        mockMvc.perform(patch("/tickets/" + 1L + "/comments/" + 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 티켓 댓글 수정 시 예외 발생")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_ThrowException_When_CommentNotFound() throws Exception {
        // given
        TicketCommentRequest request = new TicketCommentRequest("Updated Comment");

        // when & then
        mockMvc.perform(patch("/tickets/1/comments/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("정상적인 티켓 댓글 삭제")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_DeleteTicketComment_When_ValidId() throws Exception {

        mockMvc.perform(delete("/tickets/" + 1L + "/comments/" + 3L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 티켓 댓글 삭제 시 예외 발생")
    @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
    void should_ThrowException_When_DeleteNonExistentComment() throws Exception {
        // when & then
        mockMvc.perform(delete("/tickets/1/comments/9999"))
                .andExpect(status().isNotFound());
    }
}
