package com.trillion.tikitaka.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.domain.Review;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;
import com.trillion.tikitaka.ticket.infrastructure.ReviewRepository;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.context.ActiveProfiles;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * 🎯 티켓 검토 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@WithMockUser(username = "testReviewer", authorities = {"MANAGER"})
public class ReviewIntegrationTest {



    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long ticketId;
    private Long reviewerId;
    private String validToken;

    @BeforeEach
    void setUp() {
        User reviewer = User.builder()
                .username("testReviewer")
                .email("admin@dktechin.co.kr")
                .password("Password1234!")
                .role(Role.MANAGER)
                .build();
        userRepository.save(reviewer);
        reviewerId = reviewer.getId();

// SecurityContext에 CustomUserDetails 수동 주입
        CustomUserDetails userDetails = new CustomUserDetails(reviewer);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        validToken = jwtUtil.createJwtToken(
                JwtUtil.TOKEN_TYPE_ACCESS,
                reviewer.getId(),
                reviewer.getUsername(),
                reviewer.getRole().toString(),
                JwtUtil.ACCESS_TOKEN_EXPIRATION
        );

        TicketType ticketType = TicketType.builder()
                .name("Bug")
                .build();
        ticketTypeRepository.save(ticketType);

        Ticket ticket = Ticket.builder()
                .title("테스트 티켓")
                .description("Sample description")
                .status(Ticket.Status.REVIEW)
                .deadline(LocalDateTime.now().plusDays(1))
                .ticketType(ticketType)
                .requester(reviewer)
                .urgent(false)
                .build();
        ticketRepository.save(ticket);
        ticketId = ticket.getId();
    }

    @Test
    @DisplayName("✅ 검토 요청이 정상적으로 수행된다.")
    void should_ReviewTicket_When_ValidRequest() throws Exception {
        mockMvc.perform(post("/tickets/{ticketId}/reviews", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("티켓 검토를 완료했습니다."));

        List<ReviewListResponse> review = reviewRepository.findAllByTicketId(ticketId);
        assertThat(review).isNotNull();
        assertThat(review).hasSize(1);
    }


    @Test
    @DisplayName("❌ 존재하지 않는 티켓 검토 시 예외 발생")
    void should_ThrowException_When_TicketDoesNotExist() throws Exception {
        Long nonExistentTicketId = 999L; // 존재하지 않는 티켓

        mockMvc.perform(post("/tickets/{ticketId}/reviews", nonExistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 티켓을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("❌ 이미 검토된 티켓을 다시 검토하면 예외 발생")
    void should_ThrowException_When_ReviewAlreadyExists() throws Exception {
        Review review = Review.builder()
                .ticket(ticketRepository.findById(ticketId).orElseThrow())
                .reviewer(userRepository.findById(reviewerId).orElseThrow())
                .build();
        reviewRepository.save(review);

        mockMvc.perform(post("/tickets/{ticketId}/reviews", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 검토한 티켓입니다."));
    }

    @Test
    @DisplayName("✅ 검토 내역 조회가 정상적으로 수행된다.")
    void should_ReturnReviewList_When_TicketHasReviews() throws Exception {
        Review review = Review.builder()
                .ticket(ticketRepository.findById(ticketId).orElseThrow())
                .reviewer(userRepository.findById(reviewerId).orElseThrow())
                .build();
        reviewRepository.save(review);

        mockMvc.perform(get("/tickets/{ticketId}/reviews", ticketId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("검토 목록이 조회되었습니다."))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reviewerName").value("testReviewer"));
    }
}
