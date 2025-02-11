package com.trillion.tikitaka.authentication;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.domain.JwtToken;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_ACCESS;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_REFRESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("로그아웃 통합 테스트")
public class LogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @BeforeEach
    void setUp() {
        jwtTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("로그아웃 요청 시 쿠키가 존재하지 않으면 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_CookieDoesNotExist() throws Exception {
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리프레시 토큰이 만료된 경우 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_RefreshTokenIsExpired() throws Exception {
        // given
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 100L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

        Thread.sleep(150);

        // when & then
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(refreshCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리프레시 토큰 타입이 잘못된 경우 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_RefreshTokenIsInvalid() throws Exception {
        // given
        String wrongTypeToken = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 86400000L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, wrongTypeToken);

        // when & then
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(refreshCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("저장된 리프레시 토큰이 없다면 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_RefreshTokenIsNotStored() throws Exception {
        // given
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

        // when & then
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(refreshCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리프레시 토큰이 유효하고 저장되어 있다면 토큰을 삭제하고 쿠키를 만료시킨 후 로그아웃을 수행한다.")
    void should_LogoutSuccessfully_when_RefreshTokenValid() throws Exception {
        // given
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);
        JwtToken token = JwtToken.builder()
                .refreshToken(refreshToken)
                .username("user")
                .build();

        jwtTokenRepository.save(token);

        // when
        MvcResult result = mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andReturn();

        // then
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).hasSize(1);
        Cookie resultCookie = cookies[0];
        assertThat(resultCookie.getName()).isEqualTo(TOKEN_TYPE_REFRESH);
        assertThat(resultCookie.getMaxAge()).isEqualTo(0);

        assertThat(jwtTokenRepository.existsByRefreshToken(refreshToken)).isFalse();
    }
}
