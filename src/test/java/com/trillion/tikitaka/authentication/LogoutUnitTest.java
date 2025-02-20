package com.trillion.tikitaka.authentication;

import com.trillion.tikitaka.authentication.application.filter.CustomLogoutFilter;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_ACCESS;
import static com.trillion.tikitaka.authentication.application.util.JwtUtil.TOKEN_TYPE_REFRESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("로그아웃 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class LogoutUnitTest {

    @Mock
    private JwtTokenRepository jwtTokenRepository;

    private JwtUtil jwtUtil;

    @InjectMocks
    private CustomLogoutFilter logoutFilter;

    @BeforeEach
    void setUp() {
        String TEST_SECRET = "testSecretKeyForJwtShouldBeLongEnoughThisIsATestSecretKeyForUnitTestForTokenUsage";
        jwtUtil = new JwtUtil(TEST_SECRET, jwtTokenRepository);
        logoutFilter = new CustomLogoutFilter(jwtUtil, jwtTokenRepository);
    }

    @Test
    @DisplayName("쿠키가 존재하지 않으면 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_CookieDoesNotExist() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setContentType("application/json");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        logoutFilter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("리프레시 토큰이 만료된 경우 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_RefreshTokenIsExpired() throws ServletException, IOException, InterruptedException {
        // given
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 100L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setContentType("application/json");
        request.setCookies(refreshCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        Thread.sleep(150);

        // when
        logoutFilter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("리프레시 토큰 타입이 잘못된 경우 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_RefreshTokenIsInvalid() throws ServletException, IOException {
        // given
        String wrongTypeToken = jwtUtil.createJwtToken(TOKEN_TYPE_ACCESS, 1L, "user", "ROLE_USER", 86400000L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, wrongTypeToken);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setContentType("application/json");
        request.setCookies(refreshCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        logoutFilter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("저장된 리프레시 토큰이 없다면 BAD_REQUEST를 반환한다.")
    void should_ReturnBadRequest_when_RefreshTokenIsNotStored() throws ServletException, IOException {
        // given
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setContentType("application/json");
        request.setCookies(refreshCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtTokenRepository.existsByRefreshToken(refreshToken)).thenReturn(false);

        // when
        logoutFilter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("리프레시 토큰이 유효하고 저장되어 있다면 토큰을 삭제하고 쿠키를 만료시킨 후 로그아웃을 수행한다.")
    void should_LogoutSuccessfully_when_RefreshTokenValid() throws IOException, ServletException {
        // given
        String refreshToken = jwtUtil.createJwtToken(TOKEN_TYPE_REFRESH, 1L, "user", "ROLE_USER", 86400000L);
        Cookie refreshCookie = new Cookie(TOKEN_TYPE_REFRESH, refreshToken);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setContentType("application/json");
        request.setCookies(refreshCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtTokenRepository.existsByRefreshToken(refreshToken)).thenReturn(true);

        // when
        logoutFilter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

        Cookie[] cookies = response.getCookies();
        assertThat(cookies).hasSize(1);
        assertThat(cookies[0].getName()).isEqualTo(TOKEN_TYPE_REFRESH);
        assertThat(cookies[0].getMaxAge()).isEqualTo(0);

        verify(jwtTokenRepository, times(1)).deleteByRefreshToken(refreshToken);
    }
}
