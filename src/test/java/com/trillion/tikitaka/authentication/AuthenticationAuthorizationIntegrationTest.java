package com.trillion.tikitaka.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.global.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("인증/인가 유닛 테스트")
public class AuthenticationAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("인증되지 않은 사용자가 보호된 엔드포인트에 접근하면 401 UNAUTHORIZED 응답을 반환한다.")
    void should_Return401_when_UnauthenticatedAccess() throws Exception {
        // given
        String responseBody = mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // when & then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("인증은 되었지만 권한이 부족한 사용자가 /admin 엔드포인트에 접근하면 403 FORBIDDEN 응답을 반환한다.")
    @WithMockUser(username = "user", roles = {"USER"})
    void should_Return403_when_AccessWithoutSufficientRole() throws Exception {
        // given
        String responseBody = mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        // when & then
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}

