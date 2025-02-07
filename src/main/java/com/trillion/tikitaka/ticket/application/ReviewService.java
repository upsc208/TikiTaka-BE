package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.ticket.domain.Review;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.exception.TicketReviewAlreadyDoneException;
import com.trillion.tikitaka.ticket.exception.TicketReviewNotRequiredException;
import com.trillion.tikitaka.ticket.infrastructure.ReviewRepository;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public void doReview(Long ticketId, Long userId) {
        log.info("[티켓 검토] 티켓 ID: {}, 검토자 ID: {}", ticketId, userId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        if (ticket.getStatus() != Ticket.Status.REVIEW) {
            log.error("[티켓 검토] 검토가 필요없는 티켓");
            throw new TicketReviewNotRequiredException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (reviewRepository.existsByTicketAndReviewer(ticket, user)) {
            log.error("[티켓 검토] 이미 검토한 티켓");
            throw new TicketReviewAlreadyDoneException();
        }

        Review review = Review.builder()
                .ticket(ticket)
                .reviewer(user)
                .build();
        reviewRepository.save(review);
    }

    public List<ReviewListResponse> getReviews(Long ticketId) {
        log.info("[티켓 검토] 티켓 ID: {}", ticketId);
        boolean existTicket = ticketRepository.existsById(ticketId);
        if (!existTicket) {
            throw new TicketNotFoundException();
        }
        return reviewRepository.findAllByTicketId(ticketId);
    }
}
