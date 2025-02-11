package com.trillion.tikitaka.authentication;

import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("로그인 통합 테스트")
public class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
                .username("user")
                .email("user@email.com")
                .password(passwordEncoder.encode("Password1234!"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("올바른 아이디와 비밀번호를 입력하면 로그인에 성공하고 토큰을 반환한다.")
    void should_LoginSuccess_when_CorrectUsernameAndPassword() throws Exception {
        // given
        String requestBody = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"Password1234!\"\n" +
                "}";

        // when
        ResultActions resultActions = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refresh"));
    }

    @Test
    @DisplayName("잘못된 비밀번호 입력 시 로그인 실패 및 로그인 실패 횟수 증가")
    void should_FailLogin_when_WrongPassword() throws Exception {
        // given
        String requestBody = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"WrongPassword\"\n" +
                "}";

        // when
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // then
        User user = userRepository.findByUsername("user").orElseThrow();
        assertThat(user.getLoginFailCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("잘못된 아이디 입력 시 로그인 실패")
    void should_FailLogin_when_WrongUsername() throws Exception {
        // given
        String requestBody = "{\n" +
                "  \"username\": \"wrongUser\",\n" +
                "  \"password\": \"Password1234!\"\n" +
                "}";

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 실패가 누적되어 계정이 잠금되면, 잠금 상태에서 로그인 시도 시 실패한다.")
    void should_ThrowLockedException_when_AccountLocked() throws Exception {
        // given
        for (int i = 0; i < 5; i++) {
            String requestBody = "{\n" +
                    "  \"username\": \"user\",\n" +
                    "  \"password\": \"WrongPassword\"\n" +
                    "}";
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }


        User user = userRepository.findByUsername("user").orElseThrow();
        assertThat(user.isLocked()).isTrue();

        String requestBody = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"Password1234!\"\n" +
                "}";

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("계정 잠금 상태에서 잠금 만료 후 정상 로그인에 성공한다.")
    void should_LoginSuccessfully_when_LockExpired() throws Exception {
        // given
        User user = userRepository.findByUsername("user").orElseThrow();
        for (int i = 0; i < 5; i++) {
            String requestBody = "{\n" +
                    "  \"username\": \"user\",\n" +
                    "  \"password\": \"WrongPassword\"\n" +
                    "}";
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);
        ReflectionTestUtils.setField(user, "lockExpireAt", pastTime);
        userRepository.save(user);

        // when & then
        String requestBody = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"Password1234!\"\n" +
                "}";
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refresh"));
    }

    @Test
    @DisplayName("정상적인 아이디와 비밀번호를 입력하면 로그인에 성공한다.")
    void should_LoginSuccessfully_when_CorrectUsernameAndPassword() throws Exception {
        // given
        String requestBody = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"Password1234!\"\n" +
                "}";

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refresh"));
    }
}
