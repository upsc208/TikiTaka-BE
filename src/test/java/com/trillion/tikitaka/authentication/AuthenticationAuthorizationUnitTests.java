package com.trillion.tikitaka.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.handler.*;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("인증/인가 유닛 테스트")
public class AuthenticationAuthorizationUnitTests {

    @Nested
    @DisplayName("로그인 테스트")
    class DescribeLogin {

        @Mock
        private UserRepository userRepository;

        private CustomAuthenticationProvider provider;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            provider = new CustomAuthenticationProvider(userRepository);

            provider.setUserDetailsService(username -> {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("아이디 없음"));
                return new CustomUserDetails(user);
            });
        }

        @Test
        @DisplayName("아이디, 비밀번호가 맞을 경우 인증에 성공한다.")
        void should_Login_when_correctUsernameAndPassword() {
            // given
            User user = new User("testUser", "testUser@test.com", "{noop}password", Role.USER);
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken("testUser", "password");

            // when
            var result = provider.authenticate(token);

            // then
            assertThat(result).isNotNull();
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("아이디가 틀린 경우 인증에 실패한다.")
        void should_FailToLogin_when_Username() {
            // given
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken("testUser", "password");

            // when & then
            assertThatThrownBy(() -> provider.authenticate(token))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("비밀번호가 틀린 경우 인증에 실패하고 로그인 실패 횟수가 증가한다.")
        void should_FailToLogin_when_WrongPassword() {
            // given
            User user = new User("testUser", "testUser@test.com", "{noop}password", Role.USER);
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken("testUser", "wrongPW");

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
            User user = new User("testUser", "testUser@test.com", "{noop}password", Role.USER);
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

            for (int i = 0; i < 5; i++) {
                user.handleLoginFailure();
            }

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken("testUser", "wrongPW");

            // when & then
            assertThat(user.isLocked()).isTrue();
        }

        @Test
        @DisplayName("계정 잠금 상태에서 잠금 만료 전 로그인 시도 시 실패한다.")
        void should_FailToLogin_when_AccountIsLockedAndLockExpiresAtNotReached() {
            // given
            User user = new User("testUser", "testUser@test.com", "{noop}password", Role.USER);
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

            for (int i = 0; i < 5; i++) {
                user.handleLoginFailure();
            }

            assertThat(user.isLocked()).isTrue();
            assertThat(user.getLockExpireAt()).isNotNull();
            assertThat(user.getLockExpireAt()).isAfter(LocalDateTime.now());

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken("testUser", "password");

            // when & then
            assertThatThrownBy(() -> provider.authenticate(token))
                    .isInstanceOf(LockedException.class);
        }

        @Test
        @DisplayName("계정 잠금 상태에서 잠금 만료 후 로그인 시도 시 로그인에 성공한다.")
        void should_Login_when_AccountIsLockedAndLockExpiresAtReached() {
            // given
            User user = new User("testUser", "testUser@test.com", "{noop}password", Role.USER);
            when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

            for (int i = 0; i < 5; i++) {
                user.handleLoginFailure();
            }

            LocalDateTime lockExpireTime = LocalDateTime.now().minusMinutes(1);
            ReflectionTestUtils.setField(user, "lockExpireAt", lockExpireTime);

            assertThat(user.isLocked()).isTrue();
            assertThat(user.getLockExpireAt()).isBefore(LocalDateTime.now());

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken("testUser", "password");

            // when
            Authentication result = provider.authenticate(token);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPrincipal()).isInstanceOf(CustomUserDetails.class);

            CustomUserDetails customUserDetails = (CustomUserDetails) result.getPrincipal();
            assertThat(customUserDetails.getUser()).isEqualTo(user);

            verify(userRepository, times(1)).save(user);
        }
    }

    @Nested
    @DisplayName("토큰 테스트")
    class DescribeToken {

        @Mock
        private JwtTokenRepository jwtTokenRepository;

        private JwtUtil jwtUtil;

        private final String SECRET = "testSecretKeyForJwtShouldBeLongEnough1234thisIsASecretKeyForUnitTestForTokenUsage";

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            jwtUtil = new JwtUtil(SECRET, jwtTokenRepository);
        }

        @Test
        @DisplayName("JWT 토큰이 정상적으로 생성되고, 만료되지 않았으며, 타입이 올바르게 확인된다.")
        void should_BeValid_when_TokenCorrectlyCreated() {
            // given
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "testUser", "ROLE_USER", 10000L);

            // when
            boolean expired = jwtUtil.isExpired(token);
            String type = jwtUtil.getType(token);

            // then
            assertThat(expired).isFalse();
            assertThat(type).isEqualTo(TOKEN_TYPE_ACCESS);
        }

        @Test
        @DisplayName("만료된 토큰으로 요청했을 때 토큰 만료 예외가 발생한다.")
        void should_ThrowTokenExpiredException_when_TokenExpired() throws InterruptedException {
            // given
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "testUser", "ROLE_USER", 150L);
            Thread.sleep(150);

            //when & then
            assertThatThrownBy(() -> jwtUtil.getType(token))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰으로 요청했을 때 토큰 파싱 예외가 발생한다.")
        void should_MalformedJwtException_when_TokenInvalid() {
            // given
            String malformedToken = "invalidToken";

            // when & then
            assertThatThrownBy(() -> jwtUtil.getType(malformedToken))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("서명이 다른 토큰으로 요청했을 때 토큰 서명 예외가 발생한다.")
        void should_SignatureException_when_TokenInvalid() {
            // given
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "testUser", "ROLE_USER", 10000L);
            jwtUtil = new JwtUtil("differentTestSecretKeyForJwtShouldBeLongEnough1234thisIsASecretKeyForUnitTestForTokenUsage", jwtTokenRepository);

            // when & then
            assertThatThrownBy(() -> jwtUtil.getType(token))
                    .isInstanceOf(SignatureException.class);
        }
    }

    @Nested
    @DisplayName("인증/인가 테스트")
    class DescribeAuthenticationAuthorization {

        private SecurityErrorResponder errorResponder;
        private CustomAuthenticationEntryPoint entryPoint;
        private CustomAccessDeniedHandler accessDeniedHandler;

        @BeforeEach
        void setUp() {
            this.errorResponder = new SecurityErrorResponderImpl();
            entryPoint = new CustomAuthenticationEntryPoint(errorResponder);
            accessDeniedHandler = new CustomAccessDeniedHandler(errorResponder);
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 접근시 401 UNAUTHORIZED 응답을 반환한다.")
        void should_Return401_when_UnauthenticatedUserAccess() throws IOException {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            entryPoint.commence(request, response, new AuthenticationException("Unauthorized") {});

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            String body = response.getContentAsString();
            ErrorResponse error = new ObjectMapper().readValue(body, ErrorResponse.class);
            assertThat(error.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getErrorCode());
        }

        @Test
        @DisplayName("인가되지 않은 사용자가 접근시 403 FORBIDDEN 응답을 반환한다.")
        void should_Return403_when_UnauthorizedUserAccess() throws IOException {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden") {});

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
            String body = response.getContentAsString();
            ErrorResponse error = new ObjectMapper().readValue(body, ErrorResponse.class);
            assertThat(error.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED.getErrorCode());
        }
    }
}
