package com.trillion.tikitaka.user.infrastructure;

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
                        user.role
                ))
                .from(user)
                .fetch();

        return new UserListResponse(users, countAdmin(), countManager(), countUser());
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
