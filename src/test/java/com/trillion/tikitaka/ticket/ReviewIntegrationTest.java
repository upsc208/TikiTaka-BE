package com.trillion.tikitaka.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.domain.Review;
import com.trillion.tikitaka.ticket.infrastructure.CustomReviewRepositoryImpl;
import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;
import com.trillion.tikitaka.ticket.infrastructure.ReviewRepository;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 🎯 티켓 검토 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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
    private ObjectMapper objectMapper;

    private Long ticketId;
    private Long reviewerId;

    @BeforeEach
    void setUp() {
        User reviewer = User.builder()
                .username("testReviewer")
                .email("reviewer@test.com")
                .build();
        userRepository.save(reviewer);
        reviewerId = reviewer.getId();

        Ticket ticket = Ticket.builder()
                .title("테스트 티켓")
                .status(Ticket.Status.REVIEW)
                .build();
        ticketRepository.save(ticket);
        ticketId = ticket.getId();
    }

    @Test
    @DisplayName("✅ 검토 요청이 정상적으로 수행된다.")
    void should_ReviewTicket_When_ValidRequest() throws Exception {
        mockMvc.perform(post("/tickets/{ticketId}/reviews", ticketId)
                        .header("Authorization", "Bearer 테스트토큰")
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
                        .header("Authorization", "Bearer 테스트토큰")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 티켓을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("❌ 이미 검토된 티켓을 다시 검토하면 예외 발생")
    void should_ThrowException_When_ReviewAlreadyExists() throws Exception {
        // 먼저 검토를 등록
        Review review = Review.builder()
                .ticket(ticketRepository.findById(ticketId).orElseThrow())
                .reviewer(userRepository.findById(reviewerId).orElseThrow())
                .build();
        reviewRepository.save(review);

        // 동일한 티켓에 다시 검토 요청
        mockMvc.perform(post("/tickets/{ticketId}/reviews", ticketId)
                        .header("Authorization", "Bearer 테스트토큰")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 검토한 티켓입니다."));
    }

    @Test
    @DisplayName("✅ 검토 내역 조회가 정상적으로 수행된다.")
    void should_ReturnReviewList_When_TicketHasReviews() throws Exception {
        // 검토 내역 추가
        Review review = Review.builder()
                .ticket(ticketRepository.findById(ticketId).orElseThrow())
                .reviewer(userRepository.findById(reviewerId).orElseThrow())
                .build();
        reviewRepository.save(review);

        // 검토 내역 조회 요청
        mockMvc.perform(get("/tickets/{ticketId}/reviews", ticketId)
                        .header("Authorization", "Bearer 테스트토큰"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("검토 목록이 조회되었습니다."))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reviewerName").value("testReviewer"));
    }
}
