package com.trillion.tikitaka.ticketcomment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticketcomment.domain.TicketComment;
import com.trillion.tikitaka.ticketcomment.dto.request.TicketCommentRequest;
import com.trillion.tikitaka.ticketcomment.infrastructure.TicketCommentRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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


    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        ticketCommentRepository.deleteAll();


        manager1 = userRepository.saveAndFlush(User.builder()
                .username("manager1")
                .email("manager1@test.com")
                .password("manager1pass")
                .role(Role.MANAGER)
                .build());

        manager2 = userRepository.saveAndFlush(User.builder()
                .username("manager2")
                .email("manager2@test.com")
                .password("manager2pass")
                .role(Role.MANAGER)
                .build());

        normalUser1 = userRepository.saveAndFlush(User.builder()
                .username("normalUser1")
                .email("user1@test.com")
                .password("user1pass")
                .role(Role.USER)
                .build());

        normalUser2 = userRepository.saveAndFlush(User.builder()
                .username("normalUser2")
                .email("user2@test.com")
                .password("user2pass")
                .role(Role.USER)
                .build());

        admin1 = userRepository.saveAndFlush(User.builder()
                .username("admin1")
                .email("admin1@test.com")
                .password("admin1pass")
                .role(Role.ADMIN)
                .build());


        userRepository.saveAndFlush(manager1);
        userRepository.saveAndFlush(manager2);
        userRepository.saveAndFlush(normalUser1);
        userRepository.saveAndFlush(normalUser2);
        userRepository.saveAndFlush(admin1);

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

        ticketRepository.saveAndFlush(ticket1);
        ticketRepository.saveAndFlush(ticket2);
        TicketComment comment1 = ticketCommentRepository.saveAndFlush(TicketComment.builder()
                .content("첫 번째 댓글입니다.")
                .ticket(ticket1)
                .author(manager1)
                .build());

        TicketComment comment2 = ticketCommentRepository.saveAndFlush(TicketComment.builder()
                .content("두 번째 댓글입니다.")
                .ticket(ticket1)
                .author(normalUser1)
                .build());

        TicketComment comment3 = ticketCommentRepository.saveAndFlush(TicketComment.builder()
                .content("세 번째 댓글입니다.")
                .ticket(ticket2)
                .author(manager1)
                .build());
        ticketCommentRepository.flush();
    }

    @Test
    @DisplayName("정상적인 티켓 댓글 생성")
    void should_CreateTicketComment_When_ValidRequest() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();

        CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

        TicketCommentRequest request = new TicketCommentRequest("This is a test comment.");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/tickets/" + ticketId + "/comments")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(customUserDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 티켓에 댓글 생성 시 예외 발생")
    void should_ThrowException_When_TicketNotFound() throws Exception {
        // given
        TicketCommentRequest request = new TicketCommentRequest("Invalid Ticket ID");

        CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

        // when & then
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/tickets/" + 999L + "/comments")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(customUserDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("유효하지 않은 유저가 댓글을 생성할 경우 예외 발생")
    void should_ThrowException_When_InvalidUserCreatesComment() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();

        CustomUserDetails customUserDetails = new CustomUserDetails(normalUser2);

        TicketCommentRequest request = new TicketCommentRequest("Unauthorized Comment Attempt");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/tickets/" + ticketId + "/comments")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(customUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("정상적인 티켓 댓글 조회")
    void should_GetTicketComments_When_ValidRequest() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

        mockMvc.perform(get("/tickets/" + ticketId + "/comments")
                        .with(user(customUserDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("티켓 댓글 수정 - 정상 수정")
    void should_UpdateTicketComment_When_ValidRequest() throws Exception {
        // given
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        TicketCommentRequest request = new TicketCommentRequest("Updated Comment");
        TicketComment comment = ticketCommentRepository.findAll().stream().findFirst().orElseThrow();
        CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

        // when & then
        mockMvc.perform(patch("/tickets/" + ticketId + "/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customUserDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 티켓 댓글 수정 시 예외 발생")
    void should_ThrowException_When_CommentNotFound() throws Exception {
        // given
        TicketCommentRequest request = new TicketCommentRequest("Updated Comment");

        CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

        // when & then
        mockMvc.perform(patch("/tickets/1/comments/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customUserDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("정상적인 티켓 댓글 삭제")
    void should_DeleteTicketComment_When_ValidId() throws Exception {
        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
        Long ticketId = ticket.getId();
        CustomUserDetails customUserDetails = new CustomUserDetails(manager1);
        TicketComment comment = ticketCommentRepository.findAll().stream().findFirst().orElseThrow();
        mockMvc.perform(delete("/tickets/" + ticketId + "/comments/" + comment.getId())
                        .with(user(customUserDetails)))
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
