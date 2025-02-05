package com.trillion.tikitaka.user.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.dto.response.QUserResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.user.domain.QUser.user;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public UserListResponse findAllUser() {
        List<UserResponse> users = queryFactory
                .select(new QUserResponse(
                        user.id.as("userId"),
                        user.username,
                        user.email,
                        user.role,
                        Expressions.nullExpression(String.class)
                ))
                .from(user)
                .fetch();

        return new UserListResponse(users, countAdmin(), countManager(), countUser());
    }

    @Override
    public UserResponse getUserResponse(Long userId) {
        return queryFactory
                .select(new QUserResponse(
                        user.id.as("userId"),
                        user.username,
                        user.email,
                        user.role,
                        user.profileImageUrl
                ))
                .from(user)
                .where(userIdEq(userId))
                .fetchOne();
    }

    private static BooleanExpression userIdEq(Long userId) {
        return user.id.eq(userId);
    }

    public Long countAdmin() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(user.role.eq(Role.ADMIN))
                .fetchOne();
    }

    public Long countManager() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(user.role.eq(Role.MANAGER))
                .fetchOne();
    }

    public Long countUser() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(user.role.eq(Role.USER))
                .fetchOne();
    }
}
