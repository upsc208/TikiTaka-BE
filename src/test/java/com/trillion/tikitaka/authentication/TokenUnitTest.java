package com.trillion.tikitaka.authentication;

import com.trillion.tikitaka.authentication.application.JwtService;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.dto.response.TokenResponse;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import com.trillion.tikitaka.global.exception.CustomException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_ACCESS;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_REFRESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("토큰 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class TokenUnitTest {

    @Mock
    private JwtTokenRepository jwtTokenRepository;

    private JwtUtil jwtUtil;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String TEST_SECRET = "testSecretKeyForJwtShouldBeLongEnoughThisIsATestSecretKeyForUnitTestForTokenUsage";
        jwtUtil = new JwtUtil(TEST_SECRET, jwtTokenRepository);
        jwtService = new JwtService(jwtUtil, jwtTokenRepository);
    }

    @Nested
    @DisplayName("토큰 유효성 테스트")
    class DescribeTokenValidation {

        @Test
        @DisplayName("토큰이 정상적으로 생성되고, 만료되지 않았으며, 타입이 올바르게 확인된다.")
        void should_BeValid_when_TokenCorrectlyCreated() {
            // given
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 10000L);

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
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 150L);
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
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 10000L);
            jwtUtil = new JwtUtil("differentTestSecretKeyForJwtShouldBeLongEnoughThisIsASecretKeyForUnitTestForTokenUsage", jwtTokenRepository);

            // when & then
            assertThatThrownBy(() -> jwtUtil.getType(token))
                    .isInstanceOf(SignatureException.class);
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class DescribeTokenReissue {

        @Test
        @DisplayName("요청 쿠키에 리프레시 토큰이 없으면 예외를 발생시킨다.")
        void should_ThrowException_when_RefreshTokenNotFoundInCookie() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> jwtService.reissueTokens(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("리프레시 토큰이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("요청한 리프리시 토큰이 저장되어 있지 않으면 예외를 반환한다.")
        void should_ThrowException_when_RefreshTokenNotFoundInRepository() {
            // given
            String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
            Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
            when(jwtTokenRepository.existsByRefreshToken(refreshToken)).thenReturn(false);


            // then
            assertThatThrownBy(() -> jwtService.reissueTokens(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("리프레시 토큰이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("만료된 리프레시 토큰일 경우 예외가 발생한다.")
        void should_ThrowException_when_RefreshTokenExpired() throws InterruptedException {
            // given
            String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 100L);
            Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            Thread.sleep(150);

            // when & then
            assertThatThrownBy(() -> jwtService.reissueTokens(request))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("유효한 리프레시 토큰이 존재하면 새로운 토큰이 발급된다.")
        void should_ReissueTokens_when_RefreshTokenIsValid() {
            // given
            String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
            Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

            when(jwtTokenRepository.existsByRefreshToken(refreshToken)).thenReturn(true);

            // when
            TokenResponse response = jwtService.reissueTokens(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).startsWith("ey");
            assertThat(response.getRefreshToken()).startsWith("ey");
            verify(jwtTokenRepository, times(1)).deleteByRefreshToken(refreshToken);
        }
    }
}
