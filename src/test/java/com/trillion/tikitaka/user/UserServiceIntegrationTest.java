package com.trillion.tikitaka.user;

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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // MySQL 사용
@Transactional
@DisplayName("UserService 통합 테스트")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testUser")
                .email("test@email.com")
                .password("encodedPassword") // 실제 암호화 필요
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_Success() {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("encodedPassword", "newEncodedPassword");

        // when
        userService.updatePassword(testUser.getId(), request);

        // then
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호와 동일")
    void updatePassword_Fail_SamePassword() {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("encodedPassword", "encodedPassword");

        // then
        assertThatThrownBy(() -> userService.updatePassword(testUser.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NEW_PASSWORD_NOT_CHANGED.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 존재하지 않는 사용자")
    void updatePassword_Fail_UserNotFound() {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("encodedPassword", "newEncodedPassword");

        // then
        assertThatThrownBy(() -> userService.updatePassword(9999L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser_Success() {
        // when
        userService.deleteUser(testUser.getId());

        // then
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자")
    void deleteUser_Fail_NotFound() {
        assertThatThrownBy(() -> userService.deleteUser(9999L))
                .isInstanceOf(UserNotFoundException.class);
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
