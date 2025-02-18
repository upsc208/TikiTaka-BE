package com.trillion.tikitaka.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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


        userRepository.flush();

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

        ticketRepository.flush();
    }

    @Nested
    @DisplayName("티켓 생성 테스트")
    class DescribeCreateTicket {

        @Test
        @DisplayName("유효한 요청이 들어왔을 때, 티켓을 정상적으로 생성한다.")
        void should_CreateTicket_when_ValidRequest() throws Exception {
            // given
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
        @DisplayName("옵션 값(카테고리, 담당자 등)이 비어있을 때 티켓을 생성한다.")
        void should_CreateTicket_When_OptionalFieldsAreEmpty() throws Exception {
            // given
            CreateTicketRequest request = CreateTicketRequest.builder()
                    .title("빈 옵션 티켓 제목")
                    .description("빈 옵션 티켓 상세 내용")
                    .typeId(ticketType1.getId())
                    .firstCategoryId(null)
                    .secondCategoryId(null)
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
            Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
            Long ticketId = ticket.getId();

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            String responseBody = mockMvc.perform(
                            get("/tickets/{ticketId}", ticketId)
                                    .with(user(customUserDetails))
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // then
            assertThat(responseBody).contains("TicketA");
            assertThat(responseBody).contains("Desc A");
            assertThat(responseBody).contains("기본 티켓 유형");
            assertThat(responseBody).contains("카테고리A");
            assertThat(responseBody).contains("카테고리A-1");
            assertThat(responseBody).contains("2025-05-05 12:00");
            assertThat(responseBody).contains("false");
            assertThat(responseBody).contains("normalUser1");
            assertThat(responseBody).contains("manager1");

        }

            @Test
            @DisplayName("티켓 조회시 긴급 티켓이 우선적으로 조회된다.")
            void should_PrioritizeUrgentTicketsInTicketList() throws Exception {

                Ticket urgentTicket1 = ticketRepository.save(Ticket.builder()
                        .title("긴급 티켓 1")
                        .description("긴급 티켓 상세 내용")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(3))
                        .urgent(true)
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());

                Ticket urgentTicket2 = ticketRepository.save(Ticket.builder()
                        .title("긴급 티켓 2")
                        .description("긴급 티켓 상세 내용")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(4))
                        .urgent(true)
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());

                Ticket normalTicket1 = ticketRepository.save(Ticket.builder()
                        .title("일반 티켓 1")
                        .description("일반 티켓 상세 내용")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(5))
                        .urgent(false)
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());

                Ticket normalTicket2 = ticketRepository.save(Ticket.builder()
                        .title("일반 티켓 2")
                        .description("일반 티켓 상세 내용")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(6))
                        .urgent(false)
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());

                String responseBody = mockMvc.perform(
                                get("/tickets/list")
                                        .with(user(new CustomUserDetails(normalUser1)))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode contentNode = root.path("data").path("content");
                TicketListResponse[] ticketResponses = objectMapper.treeToValue(contentNode, TicketListResponse[].class);

                // 검증
                assertThat(ticketResponses).isNotEmpty();
                assertThat(ticketResponses[0].getUrgent()).isTrue();

                assertThat(responseBody).contains("긴급 티켓 1");
                assertThat(responseBody).contains("긴급 티켓 2");
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
            Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
            Long ticketId = ticket.getId();

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser2);

            String responseBody = mockMvc.perform(
                            get("/tickets/{ticketId}", ticketId)
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
                    .build());

            EditTicketRequest request = EditTicketRequest.builder()
                    .title("수정된 제목")
                    .description("수정된 상세 내용")
                    .build();
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);

            // when
            String responseBody = mockMvc.perform(
                            patch("/tickets/{ticketId}", ticket.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .with(user(customUserDetails))
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
                    .ticketTypeId(ticketType2.getId())
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
                    .ticketTypeId(ticketType2.getId())
                    .build();

            // when
            CustomUserDetails customUserDetails = new CustomUserDetails(normalUser1);
            mockMvc.perform(patch("/tickets/{ticketId}/type", ticket.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden());
        }

            @Test
            @DisplayName("담당자가 티켓 상태를 수정할 수 있다.")
            void should_EditTicketStatus_when_Manager() throws Exception {

                Ticket ticket = ticketRepository.save(Ticket.builder()
                        .title("상태 변경 테스트 티켓")
                        .description("상태 변경 전")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(5))
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());

                EditSettingRequest request = EditSettingRequest.builder()
                        .status(Ticket.Status.IN_PROGRESS)
                        .build();
                CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

                // when
                String responseBody = mockMvc.perform(
                                patch("/tickets/{ticketId}/status", ticket.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(user(customUserDetails))
                        ).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElse(null);
                assertThat(updatedTicket).isNotNull();
                assertThat(updatedTicket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
            }

            @Test
            @DisplayName("담당자가 티켓 우선순위를 수정할 수 있다.")
            void should_EditTicketPriority_when_Manager() throws Exception {
                // given
                Ticket ticket = ticketRepository.save(Ticket.builder()
                        .title("우선순위 변경 테스트 티켓")
                        .description("우선순위 변경 전")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(5))
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());
                EditSettingRequest request = EditSettingRequest.builder()
                        .priority(Ticket.Priority.HIGH)
                        .build();
                CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

                // when
                String responseBody = mockMvc.perform(
                                patch("/tickets/{ticketId}/priority", ticket.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(user(customUserDetails))
                        ).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                // then
                Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElse(null);
                assertThat(updatedTicket).isNotNull();
                assertThat(updatedTicket.getPriority()).isEqualTo(Ticket.Priority.HIGH);
            }

            @Test
            @DisplayName("담당자가 티켓 마감기한을 수정할 수 있다.")
            void should_EditTicketDeadline_when_Manager() throws Exception {
                // given
                Ticket ticket = ticketRepository.save(Ticket.builder()
                        .title("마감기한 변경 테스트 티켓")
                        .description("마감기한 변경 전")
                        .ticketType(ticketType1)
                        .firstCategory(parentCategory1)
                        .secondCategory(childCategory1)
                        .deadline(LocalDateTime.now().plusDays(5))
                        .requester(normalUser1)
                        .manager(manager1)
                        .status(Ticket.Status.PENDING)
                        .build());

                LocalDateTime newDeadline = LocalDateTime.now().plusDays(10);
                EditTicketRequest request = EditTicketRequest.builder()
                        .deadline(newDeadline)
                        .build();
                CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

                // when
                String responseBody = mockMvc.perform(
                                patch("/tickets/{ticketId}/deadline", ticket.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(user(customUserDetails))
                        ).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                // then
                Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElse(null);
                assertThat(updatedTicket).isNotNull();
                assertThat(updatedTicket.getDeadline().truncatedTo(ChronoUnit.MINUTES))
                        .isEqualTo(newDeadline.truncatedTo(ChronoUnit.MINUTES));

            }

            @Test
            @DisplayName("담당자가 티켓 담당자를 수정할 수 있다.")
            void should_EditTicketManager_when_Manager() throws Exception {
                // given
                Ticket ticket = ticketRepository.findAll().stream().findFirst().orElseThrow();
                Long ticketId = ticket.getId();

                EditSettingRequest request = EditSettingRequest.builder()
                        .managerId(manager2.getId())
                        .build();
                CustomUserDetails customUserDetails = new CustomUserDetails(manager1);

                // when
                String responseBody = mockMvc.perform(
                                patch("/tickets/{ticketId}/manager", ticket.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                        .with(user(customUserDetails))
                        ).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                // then
                Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElse(null);
                assertThat(updatedTicket).isNotNull();
                assertThat(updatedTicket.getManager().getId()).isEqualTo(manager2.getId());
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
