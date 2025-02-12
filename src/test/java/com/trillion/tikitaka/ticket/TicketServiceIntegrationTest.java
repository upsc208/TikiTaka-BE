package com.trillion.tikitaka.ticket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("티켓 서비스 통합 테스트")
public class TicketServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;


    private User manager1;
    private User manager2;
    private User normalUser1;
    private User normalUser2;

    private TicketType ticketType1;
    private TicketType ticketType2;

    private Category parentCategory1;
    private Category childCategory1;
    private Category parentCategory2;
    private Category childCategory2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        manager1 = userRepository.save(User.builder()
                .username("manager1")
                .email("manager1@test.com")
                .password("manager1pass")
                .role(Role.MANAGER)
                .build());

        manager2 = userRepository.save(User.builder()
                .username("manager2")
                .email("manager2@test.com")
                .password("manager2pass")
                .role(Role.MANAGER)
                .build());

        normalUser1 = userRepository.save(User.builder()
                .username("user1")
                .email("user1@test.com")
                .password("user1pass")
                .role(Role.USER)
                .build());

        normalUser2 = userRepository.save(User.builder()
                .username("user2")
                .email("user2@test.com")
                .password("user2pass")
                .role(Role.USER)
                .build());

        ticketType1 = ticketTypeRepository.save(new TicketType("기본 티켓 유형"));
        ticketType2 = ticketTypeRepository.save(new TicketType("두번째 티켓 유형"));

        parentCategory1 = categoryRepository.save(new Category("카테고리A", null));
        childCategory1 = categoryRepository.save(new Category("카테고리A-1", parentCategory1));

        parentCategory2 = categoryRepository.save(new Category("카테고리B", null));
        childCategory2 = categoryRepository.save(new Category("카테고리B-1", parentCategory2));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ticketRepository.save(Ticket.builder()
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

        ticketRepository.save(Ticket.builder()
                .title("TicketB")
                .description("Desc B")
                .ticketType(ticketType2)
                .firstCategory(parentCategory2)
                .secondCategory(childCategory2)
                .requester(normalUser2)
                .manager(manager1)
                .deadline(LocalDateTime.parse("2025-05-05 12:00", formatter))
                .status(Ticket.Status.PENDING)
                .build());

        ticketRepository.save(Ticket.builder()
                .title("TicketC")
                .description("Desc C")
                .ticketType(ticketType1)
                .firstCategory(parentCategory1)
                .secondCategory(childCategory1)
                .requester(normalUser1)
                .manager(manager2)
                .deadline(LocalDateTime.parse("2025-05-05 12:00", formatter))
                .status(Ticket.Status.REJECTED)
                .build());

        ticketRepository.save(Ticket.builder()
                .title("TicketD")
                .description("Desc D")
                .ticketType(ticketType2)
                .firstCategory(parentCategory2)
                .secondCategory(childCategory2)
                .requester(normalUser2)
                .manager(manager2)
                .deadline(LocalDateTime.parse("2025-05-05 12:00", formatter))
                .status(Ticket.Status.IN_PROGRESS)
                .build());
    }

    @Nested
    @DisplayName("티켓 생성 테스트")
    class DescribeCreateTicket {

        @Test
        @DisplayName("유효한 요청이 들어왔을 때, 티켓을 정상적으로 생성한다.")
        void should_CreateTicket_when_ValidRequest() throws Exception {
            // given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("테스트 티켓 제목")
                    .description("테스트 티켓 상세 내용")
                    .typeId(ticketType1.getId())
                    .firstCategoryId(parentCategory1.getId())
                    .secondCategoryId(childCategory1.getId())
                    .deadline(LocalDateTime.parse("2025-05-05 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .urgent(false)
                    .build();
            String json = objectMapper.writeValueAsString(request);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "request.json",
                    "application/json",
                    json.getBytes(StandardCharsets.UTF_8)
            );

            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            // when
            String responseBody = mockMvc.perform(
                            multipart("/tickets")
                                    .file(requestPart)
                                    .with(user(customUserDetails))
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("티켓이 생성되었습니다");
            assertThat(responseBody).contains("ticketId");
        }

        @Test
        @DisplayName("티켓 유형을 입력하지 않으면 티켓 생성에 실패한다.")
        void should_FailToCreateTicket_when_TicketTypeMissing() throws Exception {
            // given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("테스트 티켓 제목")
                    .description("테스트 티켓 상세 내용")
                    .firstCategoryId(parentCategory1.getId())
                    .secondCategoryId(childCategory1.getId())
                    .deadline(LocalDateTime.parse("2025-05-05 12:00", formatter))
                    .urgent(false)
                    .build();
            String json = objectMapper.writeValueAsString(request);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "request.json",
                    "application/json",
                    json.getBytes(StandardCharsets.UTF_8)
            );

            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            // when
            String responseBody = mockMvc.perform(
                            multipart("/tickets")
                                    .file(requestPart)
                                    .with(user(customUserDetails))
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("요청 값이 잘못되었습니다");
        }

        @Test
        @DisplayName("마감일을 입력하지 않으면 티켓 생성에 실패한다.")
        void should_FailToCreateTicket_when_DeadlineMissing() throws Exception {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("테스트 티켓 제목")
                    .description("테스트 티켓 상세 내용")
                    .typeId(ticketType1.getId())
                    .firstCategoryId(parentCategory1.getId())
                    .secondCategoryId(childCategory1.getId())
                    .urgent(false)
                    .build();
            String json = objectMapper.writeValueAsString(request);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "request.json",
                    "application/json",
                    json.getBytes(StandardCharsets.UTF_8)
            );

            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            // when
            String responseBody = mockMvc.perform(
                            multipart("/tickets")
                                    .file(requestPart)
                                    .with(user(customUserDetails))
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("요청 값이 잘못되었습니다");
        }
    }

    @Nested
    @DisplayName("티켓 조회 테스트")
    class DescribeGetTicket {

        @Test
        @DisplayName("유효한 티켓 ID를 조회하면 상세 티켓 정보를 반환한다.")
        void should_ReturnTicketDetail_when_ValidTicketId() throws Exception {
            // given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime deadline = LocalDateTime.parse("2025-05-05 12:00", formatter);

            Ticket ticket = Ticket.builder()
                    .title("테스트 티켓 제목")
                    .description("테스트 티켓 상세 내용")
                    .ticketType(ticketType1)
                    .firstCategory(parentCategory1)
                    .secondCategory(childCategory1)
                    .deadline(deadline)
                    .urgent(false)
                    .requester(normalUser1)
                    .manager(manager1)
                    .build();
            ticketRepository.save(ticket);

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            String responseBody = mockMvc.perform(
                            get("/tickets/{ticketId}", ticket.getId())
                                    .with(user(customUserDetails))
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("테스트 티켓 제목");
            assertThat(responseBody).contains("테스트 티켓 상세 내용");
            assertThat(responseBody).contains("기본 티켓 유형");
            assertThat(responseBody).contains("카테고리A");
            assertThat(responseBody).contains("카테고리A-1");
            assertThat(responseBody).contains("2025-05-05 12:00");
            assertThat(responseBody).contains("false");
            assertThat(responseBody).contains("user1");
            assertThat(responseBody).contains("manager1");
        }

        @Test
        @DisplayName("존재하지 않는 티켓 ID를 조회하면 실패한다.")
        void should_FailToGetTicket_when_InvalidTicketId() throws Exception {
            // given
            Long invalidTicketId = 9999999L;

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            String responseBody = mockMvc.perform(
                            get("/tickets/{ticketId}", invalidTicketId)
                                    .with(user(customUserDetails))
                    )
                    .andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("티켓을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("다른 사용자가 생성한 티켓을 사용자가 조회하면 실패한다.")
        void should_FailToGetTicket_when_OtherUserTicket() throws Exception {
            // given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            Ticket otherTicket = Ticket.builder()
                    .title("다른 유저 티켓")
                    .description("다른 유저 내용")
                    .ticketType(ticketType1)
                    .firstCategory(parentCategory1)
                    .secondCategory(childCategory1)
                    .deadline(LocalDateTime.parse("2025-05-05 12:00", formatter))
                    .urgent(false)
                    .requester(normalUser2) // user2
                    .manager(manager1)
                    .build();
            ticketRepository.save(otherTicket);

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            String responseBody = mockMvc.perform(
                            get("/tickets/{ticketId}", otherTicket.getId())
                                    .with(user(customUserDetails))
                    )
                    .andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("해당 티켓을 찾을 수 없습니다");
        }
    }
    @Nested
    @DisplayName("티켓 수정 테스트")
    class DescribeEditTicket {

        @Test
        @DisplayName("사용자가 유효한 요청으로 티켓을 수정할 수 있다.")
        @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")//미리 설정된 user는 인식을 못함 로컬에 저장된 user.tk는 인식가능
        void should_EditTicket_when_ValidRequest() throws Exception {
            // given
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .title("수정 전 제목")
                    .description("수정 전 상세 내용")
                    .ticketType(ticketType1)
                    .firstCategory(parentCategory1)
                    .secondCategory(childCategory1)
                    .deadline(LocalDateTime.now().plusDays(5))
                    .requester(normalUser1)
                    .manager(manager1)
                    .build());

            EditTicketRequest request = EditTicketRequest.builder()
                    .title("수정된 제목")
                    .description("수정된 상세 내용")
                    .build();

            // when
            String responseBody = mockMvc.perform(
                            patch("/tickets/{ticketId}", ticket.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("티켓이 수정되었습니다.");
        }


        @Test
        @DisplayName("담당자가 티켓 유형을 수정할 수 있다.")
        @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
        void should_EditTicketType_when_Manager() throws Exception {
            // given
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .title("제목")
                    .description("내용")
                    .ticketType(ticketType1)
                    .requester(normalUser1)
                    .manager(manager1)
                    .deadline(LocalDateTime.now().plusDays(5))
                    .build());

            EditTicketRequest request = EditTicketRequest.builder()
                    .ticketTypeId(ticketType2.getId()) // 다른 유형으로 변경
                    .build();

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(manager1);
            mockMvc.perform(
                            patch("/tickets/{ticketId}/type", ticket.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("사용자가 티켓 유형을 수정하려 하면 실패한다.")
        @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")
        void should_FailToEditTicketType_when_User() throws Exception {
            // given
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .title("제목")
                    .description("내용")
                    .ticketType(ticketType1)
                    .requester(normalUser1)
                    .manager(manager1)
                    .deadline(LocalDateTime.now())
                    .build());

            EditTicketRequest request = EditTicketRequest.builder()
                    .ticketTypeId(ticketType2.getId()) // 다른 유형으로 변경
                    .build();

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);
            mockMvc.perform(patch("/tickets/{ticketId}/type", ticket.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden());
        }
    }
    @Nested
    @DisplayName("티켓 삭제 테스트")
    class DescribeDeleteTicket {

        @Test
        @DisplayName("정상적으로 PENDING 상태의 티켓을 삭제할 수 있다.")
        @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")
        void should_DeleteTicket_when_ValidRequest() throws Exception {
            // given
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .title("삭제할 티켓")
                    .description("삭제할 티켓 내용")
                    .ticketType(ticketType1)
                    .firstCategory(parentCategory1)
                    .secondCategory(childCategory1)
                    .deadline(LocalDateTime.now().plusDays(5))
                    .requester(normalUser1)
                    .status(Ticket.Status.PENDING)
                    .build());

            // when
            mockMvc.perform(delete("/tickets/{ticketId}", ticket.getId())
                            .with(user(new CustomUserDetails(normalUser1))))
                    .andExpect(status().isOk());

            // then
            assertThat(ticketRepository.findById(ticket.getId())).isEmpty();
        }

        @Test
        @DisplayName("PENDING 상태가 아닌 티켓을 삭제하려 하면 실패한다.")
        @WithUserDetails(value = "user.tk", userDetailsServiceBeanName = "customUserDetailsService")
        void should_FailToDeleteTicket_when_StatusIsNotPending() throws Exception {
            // given
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .title("삭제 불가능한 티켓")
                    .description("진행 중 상태이므로 삭제 불가")
                    .ticketType(ticketType1)
                    .firstCategory(parentCategory1)
                    .secondCategory(childCategory1)
                    .deadline(LocalDateTime.now().plusDays(5))
                    .requester(normalUser1)
                    .status(Ticket.Status.IN_PROGRESS)
                    .build());

            // when
            mockMvc.perform(delete("/tickets/{ticketId}", ticket.getId())
                            .with(user(new CustomUserDetails(normalUser1))))
                    .andExpect(status().isForbidden());

            // then
            assertThat(ticketRepository.findById(ticket.getId())).isPresent();
        }

        @Test
        @DisplayName("담당자가 티켓을 삭제하려 하면 실패한다.")
        @WithUserDetails(value = "manager.tk", userDetailsServiceBeanName = "customUserDetailsService")
        void should_FailToDeleteTicket_when_ManagerTriesToDelete() throws Exception {
            // given
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .title("담당자가 삭제할 수 없는 티켓")
                    .description("요청자만 삭제 가능")
                    .ticketType(ticketType1)
                    .firstCategory(parentCategory1)
                    .secondCategory(childCategory1)
                    .deadline(LocalDateTime.now().plusDays(5))
                    .requester(normalUser1)
                    .manager(manager1)
                    .status(Ticket.Status.PENDING)
                    .build());

            // when
            mockMvc.perform(delete("/tickets/{ticketId}", ticket.getId())
                            .with(user(new CustomUserDetails(manager1))))
                    .andExpect(status().isForbidden());

            // then
            assertThat(ticketRepository.findById(ticket.getId())).isPresent();
        }
    }



}
