package com.trillion.tikitaka.ticket.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.ticket.dto.response.QReviewListResponse;
import com.trillion.tikitaka.ticket.dto.response.ReviewListResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.ticket.domain.QReview.review;

@RequiredArgsConstructor
public class CustomReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReviewListResponse> findAllByTicketId(Long ticketId) {
        return queryFactory
                .select(new QReviewListResponse(
                        review.id.as("reviewId"),
                        review.reviewer.username.as("reviewerName"),
                        review.createdAt
                ))
                .from(review)
                .where(review.ticket.id.eq(ticketId))
                .fetch();
    }
}
