package com.trillion.tikitaka.authentication;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.JwtToken;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_ACCESS;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_REFRESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("토큰 통합 테스트")
public class TokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jwtTokenRepository.deleteAll();

        User user = User.builder()
                .username("user")
                .email("user@email.com")
                .password(passwordEncoder.encode("Password1234!"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    @Nested
    @DisplayName("토큰 유효성 테스트")
    class TokenValidationTests {

        @Test
        @DisplayName("만료된 토큰으로 요청했을 때 토큰 만료 오류가 발생한다.")
        void should_ReturnUnauthorized_when_TokenExpired() throws Exception {
            // given
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 150L);
            Thread.sleep(200);

            // when & then
            assertThatThrownBy(() ->
                    mockMvc.perform(get("/me")
                                    .header("Authorization", "Bearer " + token))
                            .andReturn()
            ).isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰으로 요청했을 때 401 Unauthorized를 반환한다.")
        void should_ReturnUnauthorized_when_TokenMalformed() {
            // given
            String malformedToken = "invalidToken";

            // when & then
            assertThatThrownBy(() ->
                    mockMvc.perform(get("/me")
                                    .header("Authorization", "Bearer " + malformedToken))
                            .andReturn()
            ).isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("서명이 다른 토큰으로 요청했을 때 잘못된 서명 오류가 발생한다.")
        void should_ThrowSignatureException_when_TokenInvalid() {
            // given
            String token = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 10000L);
            String longSecretKey = "differentTestSecretKeyForJwtShouldBeLongEnoughForHS512Algorithm!";
            JwtUtil differentJwtUtil = new JwtUtil(longSecretKey, jwtTokenRepository);

            // when & then
            assertThatThrownBy(() -> differentJwtUtil.getType(token))
                    .isInstanceOf(SignatureException.class);
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class TokenReissueTests {

        @Test
        @DisplayName("요청 쿠키에 리프레시 토큰이 없으면 400 Bad Request를 반환한다.")
        void should_ReturnBadRequest_when_RefreshTokenNotFoundInCookie() throws Exception {
            mockMvc.perform(post("/reissue")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("요청한 리프레시 토큰이 저장되어 있지 않으면 400 Bad Request를 반환한다.")
        void should_ReturnBadRequest_when_RefreshTokenNotFoundInRepository() throws Exception {
            // given: 유효한 리프레시 토큰 생성
            String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
            Cookie refreshCookie = new Cookie("refresh", refreshToken);

            // when & then: 저장소에 해당 토큰이 없으므로 400 응답 검증
            mockMvc.perform(post("/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .cookie(refreshCookie))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("만료된 리프레시 토큰일 경우 401 Unauthorized를 반환한다.")
        void should_ReturnBadRequest_when_RefreshTokenExpired() throws Exception {
            // given
            String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 100L);
            Cookie refreshCookie = new Cookie("refresh", refreshToken);
            Thread.sleep(150);

            // when & then
            mockMvc.perform(post("/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .cookie(refreshCookie))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효한 리프레시 토큰이 존재하면 새로운 토큰이 발급된다.")
        void should_ReissueTokens_when_RefreshTokenIsValid() throws Exception {
            // given
            String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
            Cookie refreshCookie = new Cookie("refresh", refreshToken);

            JwtToken token = JwtToken.builder()
                    .refreshToken(refreshToken)
                    .username("user")
                    .build();
            jwtTokenRepository.save(token);

            // when
            MvcResult result = mockMvc.perform(post("/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("\"message\":\"토큰이 재발급 되었습니다.\",\"data\":null");

            String newAccessToken = result.getResponse().getHeader("Authorization");
            assertThat(newAccessToken).isNotNull();
            assertThat(newAccessToken).startsWith("Bearer ");

            String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
            assertThat(setCookieHeader).isNotNull();
            assertThat(setCookieHeader).contains("refresh=");
        }
    }
}
