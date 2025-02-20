package com.trillion.tikitaka.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.handler.CustomAccessDeniedHandler;
import com.trillion.tikitaka.authentication.application.handler.CustomAuthenticationEntryPoint;
import com.trillion.tikitaka.authentication.application.handler.SecurityErrorResponder;
import com.trillion.tikitaka.authentication.application.handler.SecurityErrorResponderImpl;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.global.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("인증/인가 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class AuthenticationAuthorizationUnitTests {

    private CustomAuthenticationEntryPoint entryPoint;
    private CustomAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        SecurityErrorResponder errorResponder = new SecurityErrorResponderImpl();
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
        entryPoint.commence(request, response, new AuthenticationException("UNAUTHORIZED") {});
        String body = response.getContentAsString();
        ErrorResponse error = new ObjectMapper().readValue(body, ErrorResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(error.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getErrorCode());
    }

    @Test
    @DisplayName("인가되지 않은 사용자가 접근시 403 FORBIDDEN 응답을 반환한다.")
    void should_Return403_when_UnauthorizedUserAccess() throws IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        accessDeniedHandler.handle(request, response, new AccessDeniedException("FORBIDDEN") {});
        String body = response.getContentAsString();
        ErrorResponse error = new ObjectMapper().readValue(body, ErrorResponse.class);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(error.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED.getErrorCode());
    }
}

