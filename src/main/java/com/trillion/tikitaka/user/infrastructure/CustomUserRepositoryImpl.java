package com.trillion.tikitaka.user.infrastructure;

import com.querydsl.core.BooleanBuilder;
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
    public UserListResponse getAllUsersByRole(Role targetRole, Role currentUserRole) {

        BooleanBuilder builder = new BooleanBuilder();

        if (targetRole != null) {
            builder.and(user.role.eq(targetRole));
        } else {
            if (currentUserRole == Role.ADMIN) {
                // 관리자일 경우 모든 유저 조회
            } else {
                builder.and(user.role.in(Role.MANAGER, Role.USER));
            }
        }

        List<UserResponse> users = queryFactory
                .select(new QUserResponse(
                        user.id.as("userId"),
                        user.username,
                        user.email,
                        user.role,
                        user.profileImageUrl
                ))
                .from(user)
                .where(builder)
                .fetch();

        UserListResponse response = new UserListResponse();
        response.setUsers(users);

        if (currentUserRole == Role.ADMIN) {
            response.setAdminCount(countAdmin());
            response.setManagerCount(countManager());
            response.setUserCount(countUser());
        } else {
            response.setAdminCount(null);
            response.setManagerCount(null);
            response.setUserCount(null);
        }

        return response;
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

    @Override
    public List<UserResponse> getAllUsers() {
        return queryFactory
                .select(new QUserResponse(
                        user.id.as("userId"),
                        user.username,
                        user.email,
                        user.role,
                        user.profileImageUrl
                ))
                .from(user)
                .fetch();
    }

    private static BooleanExpression userIdEq(Long userId) {
        return user.id.eq(userId);
    }

    private static BooleanExpression userRoleEq(Role role) {
        return role != null ? user.role.eq(role) : null;
    }

    public Long countAdmin() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(userRoleEq(Role.ADMIN))
                .fetchOne();
    }

    public Long countManager() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(userRoleEq(Role.MANAGER))
                .fetchOne();
    }

    public Long countUser() {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(userRoleEq(Role.USER))
                .fetchOne();
    }
}
