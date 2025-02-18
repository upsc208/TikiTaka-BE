package com.trillion.tikitaka.statistic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.history.infrastructure.HistoryRepository;
import com.trillion.tikitaka.statistics.dto.response.AllMonth;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("월간 통계 통합 테스트")
public class MonthlyStatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HistoryRepository historyRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private User user;

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

        historyRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();


        manager1 = userRepository.saveAndFlush(User.builder()
                .username("manager1")
                .email("manager1@test.com")
                .password("manager1pass")
                .role(Role.MANAGER)
                .build());

        normalUser1 = userRepository.saveAndFlush(User.builder()
                .username("normalUser1")
                .email("user1@test.com")
                .password("user1pass")
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
                .requester(normalUser1)  // ✅ 반드시 `requester` 설정
                .manager(manager1)
                .deadline(LocalDateTime.parse("2025-06-10 15:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .status(Ticket.Status.PENDING)
                .build());


        ticketRepository.flush();

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket1)
                .updateType(TicketHistory.UpdateType.STATUS_CHANGE)
                .updatedBy(admin1)
                .updatedAt(LocalDateTime.now())
                .build();

        historyRepository.saveAndFlush(history);
    }


    @Test
    @DisplayName("인증되지 않은 사용자가 월간 통계를 조회하면 401 UNAUTHORIZED 응답을 반환한다.")
    void should_Return401_When_UnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/statistic/monAll")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("MANAGER 권한을 가진 사용자는 월간 통계를 조회할 수 있다.")
    void should_ReturnMonthlyStatistics_When_ManagerUser() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(manager1);

        String responseBody = mockMvc.perform(get("/statistic/monAll")
                        .param("year", "2025")
                        .param("month", "2")
                        .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<AllMonth> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("잘못된 연도와 월을 입력하면 400 BAD REQUEST 응답을 반환한다.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void should_Return400_When_InvalidYearMonthProvided() throws Exception {
        mockMvc.perform(post("/statistic/record")
                        .param("year", "abcd")
                        .param("month", "99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN 사용자는 특정 타입별 통계를 조회할 수 있다.")
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void should_ReturnTypeStatistics_When_AdminUser() throws Exception {
        String responseBody = mockMvc.perform(get("/statistic/monType")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("MANAGER 사용자는 특정 사용자의 월간 통계를 조회할 수 있다.")
    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    void should_ReturnUserStatistics_When_ManagerUser() throws Exception {
        String responseBody = mockMvc.perform(get("/statistic/monUser")
                        .param("year", "2025")
                        .param("month", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> response = mapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("성공");
    }
}
