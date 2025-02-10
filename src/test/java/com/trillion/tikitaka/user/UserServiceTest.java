package com.trillion.tikitaka.user;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.request.PasswordChangeRequest;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.application.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 유닛 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class UpdatePassword {

        private PasswordChangeRequest request;

        @BeforeEach
        void setUp() {
            request = new PasswordChangeRequest("CurrentPassword", "newPassword");
        }

        @Test
        @DisplayName("새 비밀번호가 기존 비밀번호와 동일하면 오류 발생.")
        void should_FailToUpdatePassword_When_NewPasswordIsSameAsCurrent() {
            PasswordChangeRequest request = new PasswordChangeRequest("samePassword", "samePassword");
            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.NEW_PASSWORD_NOT_CHANGED.getMessage());
        }

        @Test
        @DisplayName("현재 비밀번호가 일치하지 않으면 오류 발생.")
        void should_FailToUpdatePassword_When_CurrentPasswordIsIncorrect() {
            User user = new User("testUser", "test@email.com",
                    "encodedPassword", Role.USER);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("CurrentPassword", user.getPassword()))
                    .thenReturn(false);
            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED.getMessage());
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class DeleteUser {

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하려 하면 오류가 발생.")
        void should_FailToDeleteUser_When_UserNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userService.deleteUser(1L))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 사용자가 정상 삭제.")
        void should_DeleteUserSuccessfully_When_UserExists() {
            User user = new User("testUser", "test@email.com",
                    "encodedPassword", Role.USER);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doNothing().when(userRepository).delete(user);

            assertThatCode(() -> userService.deleteUser(1L))
                    .doesNotThrowAnyException();
            verify(userRepository, times(1)).delete(user);
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class GetUserResponse {

        @Test
        @DisplayName("존재하지 않는 사용자의 정보를 조회하면 오류 발생.")
        void should_FailToGetUserResponse_When_UserNotFound() {
            when(userRepository.getUserResponse(1L)).thenReturn(null);
            assertThatThrownBy(() -> userService.getUserResponse(1L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
