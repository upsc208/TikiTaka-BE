package com.trillion.tikitaka.user;

import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("사용자 유닛 테스트")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, registrationRepository);
    }

    @Nested
    @DisplayName("계정 등록 대기 및 사용자 수 조회 테스트")
    class DescribeGetRegistrationAndUserCount {

        @Test
        @DisplayName("대기 상태의 계정 등록 개수와 전체 사용자 수를 조회한다.")
        void should_ReturnRegistrationAndUserCountResponse_When_GetRegistrationAndUserCount() {
            // given
            when(registrationRepository.countByStatus(RegistrationStatus.PENDING))
                    .thenReturn(5L);
            when(userRepository.count()).thenReturn(10L);

            // when
            RegistrationAndUserCountResponse response = userService.getRegistrationAndUserCount();

            // then
            assertThat(response.getRegistrationCount()).isEqualTo(5L);
            assertThat(response.getUserCount()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("전체 사용자 목록 조회 테스트")
    class DescribeGetAllUsers {

        @Test
        @DisplayName("시스템 전체 사용자 목록과 역할별 사용자 수를 조회한다.")
        void should_ReturnAllUsersWithRoleCounts_when_GetAllUsers() {
            // given
            UserListResponse mockResponse = new UserListResponse(null, 1L, 2L, 10L);
            when(userRepository.findAllUser()).thenReturn(mockResponse);

            // when
            UserListResponse result = userService.findAllUsers();

            // then
            assertThat(result.getAdminCount()).isEqualTo(1L);
            assertThat(result.getManagerCount()).isEqualTo(2L);
            assertThat(result.getUserCount()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("특정 사용자 조회 테스트")
    class DescribeGetUserResponse {

        @Test
        @DisplayName("주어진 아이디의 유저 정보를 조회해 반환한다.")
        void should_ReturnUserResponse_when_UserExists() {
            // given
            Long userId = 100L;
            UserResponse mockUserResponse = new UserResponse(userId, "username", "email@test.com", Role.USER);

            when(userRepository.getUserResponse(userId)).thenReturn(mockUserResponse);

            // when
            UserResponse result = userService.getUserResponse(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(100L);
            assertThat(result.getUsername()).isEqualTo(mockUserResponse.getUsername());
            assertThat(result.getEmail()).isEqualTo(mockUserResponse.getEmail());
        }

        @Test
        @DisplayName("주어진 아이디의 유저 정보가 없으면 오류가 발생한다.")
        void should_ThrowException_when_UserNotFound() {
            // given
            Long userId = 100L;
            when(userRepository.getUserResponse(userId)).thenReturn(null);

            // when, then
            assertThatThrownBy(() -> userService.getUserResponse(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
