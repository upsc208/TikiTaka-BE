package com.trillion.tikitaka.authentication;

import com.trillion.tikitaka.authentication.application.handler.CustomAuthenticationProvider;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("로그인 단위 테스트")
@ExtendWith(MockitoExtension.class)
public class LoginUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider.setUserDetailsService(username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));
            return new CustomUserDetails(user);
        });
    }

    @Test
    @DisplayName("아이디가 틀리거나 존재하지 않는 경우 인증에 실패한다.")
    void should_FailToLogin_when_WrongUsername() {
        // given
        when(userRepository.findByUsername("wrongUser")).thenReturn(Optional.empty());

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("wrongUser", "password");

        // when & then
        assertThatThrownBy(() -> provider.authenticate(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀린 경우 인증에 실패하고 로그인 실패 횟수가 증가한다.")
    void should_FailToLogin_when_WrongPassword() {
        // given
        User user = new User("user", "user@test.com", "{noop}password", Role.USER);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("user", "wrongPW");

        // when & then
        assertThatThrownBy(() -> provider.authenticate(token))
                .isInstanceOf(BadCredentialsException.class);
        assertThat(user.getLoginFailCount()).isEqualTo(1);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("로그인 실패가 5회 되면 계정을 잠금한다.")
    void should_LockAccount_when_FailToLoginFiveTimes() {
        // given
        User user = new User("user", "user@test.com", "{noop}password", Role.USER);

        for (int i = 0; i < 5; i++) {
            user.handleLoginFailure();
        }
        // when & then
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("계정 잠금 상태에서 잠금 만료 전 로그인 시도 시 실패한다.")
    void should_FailToLogin_when_AccountIsLockedAndLockExpiresAtNotReached() {
        // given
        User user = new User("user", "user@test.com", "{noop}password", Role.USER);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        for (int i = 0; i < 5; i++) {
            user.handleLoginFailure();
        }

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("user", "password");

        // when & then
        assertThat(user.isLocked()).isTrue();
        assertThat(user.getLockExpireAt()).isNotNull();
        assertThat(user.getLockExpireAt()).isAfter(LocalDateTime.now());
        assertThatThrownBy(() -> provider.authenticate(token))
                .isInstanceOf(LockedException.class);
    }

    @Test
    @DisplayName("아이디, 비밀번호가 알맞을 경우 로그인에 성공한다.")
    void should_Login_when_correctUsernameAndPassword() {
        // given
        User user = new User("user", "user@test.com", "{noop}password", Role.USER);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("user", "password");

        // when
        Authentication result = provider.authenticate(token);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal())
                .isInstanceOf(CustomUserDetails.class)
                .extracting("user").isEqualTo(user);
        assertThat(result.isAuthenticated()).isEqualTo(true);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("계정 잠금 상태에서 잠금 만료 후 로그인 시도 시 로그인에 성공한다.")
    void should_Login_when_AccountIsLockedAndLockExpiresAtReached() {
        // given
        User user = new User("user", "user@test.com", "{noop}password", Role.USER);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        for (int i = 0; i < 5; i++) {
            user.handleLoginFailure();
        }

        LocalDateTime lockExpireTime = LocalDateTime.now().minusMinutes(1);
        ReflectionTestUtils.setField(user, "lockExpireAt", lockExpireTime);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("user", "password");

        // when
        Authentication result = provider.authenticate(token);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal())
                .isInstanceOf(CustomUserDetails.class)
                .extracting("user").isEqualTo(user);
        verify(userRepository, times(1)).save(user);
    }
}
