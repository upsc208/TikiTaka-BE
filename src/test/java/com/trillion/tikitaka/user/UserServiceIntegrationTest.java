package com.trillion.tikitaka.user;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.request.PasswordChangeRequest;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("UserService 통합 테스트")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testUser")
                .email("test@email.com")
                .password(passwordEncoder.encode("originalPassword"))
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        userDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_Success() {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("originalPassword", "newPassword");

        // when
        userService.updatePassword(testUser.getId(), request);

        // then
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword", updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호와 동일")
    void updatePassword_Fail_SamePassword() {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("originalPassword", "originalPassword");

        // then
        assertThatThrownBy(() -> userService.updatePassword(testUser.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NEW_PASSWORD_NOT_CHANGED.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 존재하지 않는 사용자")
    void updatePassword_Fail_UserNotFound() {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("originalPassword", "newPassword");

        // then
        assertThatThrownBy(() -> userService.updatePassword(9999L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser_Success() {
        // when
        User anotherUser = User.builder()
                .username("anotherUser")
                .email("anotherUser@email.com")
                .password(passwordEncoder.encode("originalPassword"))
                .role(Role.USER)
                .build();
        userRepository.save(anotherUser);

        userService.deleteUser(anotherUser.getId(), userDetails);

        // then
        assertThatThrownBy(() -> userRepository.findById(anotherUser.getId())
                .orElseThrow(UserNotFoundException::new))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자")
    void deleteUser_Fail_NotFound() {
        assertThatThrownBy(() -> userService.deleteUser(9999L, userDetails))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 자기 자신 삭제 시도")
    void deleteUser_Fail_CannotDeleteMyself() {
        assertThatThrownBy(() -> userService.deleteUser(testUser.getId(), userDetails))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CANNOT_DELETE_MYSELF.getMessage());
    }

    @Test
    @DisplayName("사용자 삭제 실패 - userDetails가 null일 경우")
    void deleteUser_Fail_NullUserDetails() {
        assertThatThrownBy(() -> userService.deleteUser(testUser.getId(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserResponse_Success() {
        // when
        UserResponse response = userService.getUserResponse(testUser.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 존재하지 않는 사용자")
    void getUserResponse_Fail_NotFound() {
        assertThatThrownBy(() -> userService.getUserResponse(9999L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
