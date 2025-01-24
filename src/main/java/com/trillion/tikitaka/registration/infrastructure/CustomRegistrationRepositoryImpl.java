package com.trillion.tikitaka.registration.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.response.QRegistrationListResponse;
import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.trillion.tikitaka.registration.domain.QRegistration.registration;

@Repository
@RequiredArgsConstructor
public class CustomRegistrationRepositoryImpl implements CustomRegistrationRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RegistrationListResponse> getRegistrations(RegistrationStatus status, Pageable pageable) {

        List<RegistrationListResponse> content = queryFactory
                .select(new QRegistrationListResponse(
                        registration.id.as("registrationId"),
                        registration.username,
                        registration.email,
                        registration.status,
                        registration.createdAt
                ))
                .from(registration)
                .where(statusEq(status))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(registration.count())
                .from(registration)
                .where(statusEq(status));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(RegistrationStatus status) {
        return status != null ? registration.status.eq(status) : null;
    }
}
