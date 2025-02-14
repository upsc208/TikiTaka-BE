package com.trillion.tikitaka.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.registration.domain.Registration;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("계정 등록 서비스 통합 테스트")
public class RegistrationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RegistrationRepository registrationRepository;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("계정 등록 테스트")
    class DescribeCreateRegistration {

        @Test
        @DisplayName("유효한 아이디와 이메일로 계정 등록 신청을 요청하면 등록이 생성된다")
        void should_CreateRegistrationSuccessfully_when_ValidUsernameAndEmail() throws Exception {
            // given
            RegistrationRequest request = new RegistrationRequest("valid.ts", "valid@email.com");
            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/registrations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());

            List<Registration> saved = registrationRepository.findAll();
            assertThat(saved.get(0).getEmail()).isEqualTo("valid@email.com");
            assertThat(saved.get(0).getStatus()).isEqualTo(RegistrationStatus.PENDING);
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 요청하면 에러가 발생한다.")
        void should_ReturnBadRequest_when_InvalidEmailFormat() throws Exception {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "invalid-email");
            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/registrations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("중복되는 아이디로 요청하면 에러가 발생한다.")
        void should_Return400_when_AlreadyDuplicatedUsername() throws Exception {
            // given
            registrationRepository.save(Registration.builder()
                    .username("test.ts")
                    .email("test@email.com")
                    .build());

            RegistrationRequest request = new RegistrationRequest("test.ts", "new@email.com");
            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/registrations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("계정 등록 조회 테스트")
    class DescribeGetRegistrations {

        @Test
        @DisplayName("상태 필터링을 통해 계정 등록 신청 목록을 조회하면 해당 상태의 목록만 반환된다.")
        @WithMockUser(authorities = "ADMIN")
        void should_ReturnPendingRegistrations_when_StatusIsPending() throws Exception {
            // given
            Registration pending1 = registrationRepository.save(
                    Registration.builder().username("testOne.ts").email("p1@email").build()
            );
            Registration pending2 = registrationRepository.save(
                    Registration.builder().username("testTwo.ts").email("p2@email").build()
            );

            Registration approved = registrationRepository.save(
                    Registration.builder().username("testThree.ts").email("ap@email").build()
            );
            approved.approve("approved reason");

            // when & then
            mockMvc.perform(get("/registrations/list")
                            .param("page", "0")
                            .param("size", "20")
                            .param("status", "PENDING")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.data.content[1].status").value("PENDING"));
        }

        @Test
        @DisplayName("필터링을 하지 않으면 모든 계정 등록 신청 목록을 조회한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_ReturnAllRegistrations_when_StatusIsNull() throws Exception {
            // given
            Registration pending = registrationRepository.save(Registration.builder()
                    .username("pending.ts").email("p@email").build());
            Registration approved = registrationRepository.save(Registration.builder()
                    .username("approved.ts").email("a@email").build());
            approved.approve("approved reason");
            Registration rejected = registrationRepository.save(Registration.builder()
                    .username("rejected.ts").email("r@email").build());
            rejected.reject("rejected reason");

            // when & then
            mockMvc.perform(get("/registrations/list")
                            .param("page", "0")
                            .param("size", "20")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(3));
        }
    }

    @Nested
    @DisplayName("계정 등록 처리 테스트")
    class DescribeProcessRegistration {

        @Test
        @DisplayName("존재하지 않는 계정 등록 신청을 처리하려하면 오류가 발생한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_ReturnNotFound_when_RegistrationIdNotExist() throws Exception {
            // given
            Long notExistsId = 999999L;
            String jsonBody = """
                    {
                      "role": "USER",
                      "reason": "trying to approve"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/registrations/" + notExistsId)
                            .param("status", "APPROVED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("이미 처리된 계정 등록 신청을 처리하려하면 오류가 발생한다.")
        @WithMockUser(authorities = "ADMIN")
        void should_ReturnBadRequest_when_AlreadyProcessedRegistration() throws Exception {
            // given
            Registration reg = registrationRepository.save(Registration.builder()
                    .username("done.ts")
                    .email("done@email")
                    .build());
            reg.approve("done reason");

            String jsonBody = """
                    {
                      "role": "USER",
                      "reason": "another reason"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/registrations/" + reg.getId())
                            .param("status", "APPROVED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("계정 등록 신청을 승인하면 상태가 APPROVED로 변경되고 User가 생성된다.")
        @WithMockUser(authorities = "ADMIN")
        void should_ApproveRegistrationAndCreateUser_when_PendingRegistrationApproved() throws Exception {
            // given
            Registration pending = registrationRepository.save(Registration.builder()
                    .username("test.ts")
                    .email("test@email.com")
                    .build());

            String jsonBody = """
                    {
                      "role": "USER",
                      "reason": "approve reason"
                    }
                    """;

            // when
            mockMvc.perform(post("/registrations/" + pending.getId())
                            .param("status", "APPROVED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isOk());

            // then
            Registration updated = registrationRepository.findById(pending.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
            assertThat(updated.getReason()).isEqualTo("approve reason");

            User createdUser = userRepository.findByUsername("test.ts").orElseThrow();
            assertThat(createdUser.getEmail()).isEqualTo("test@email.com");
        }

        @Test
        @DisplayName("계정 등록 신청을 거부하면 상태가 REJECTED로 변경되고 User가 생성되지 않는다.")
        @WithMockUser(authorities = "ADMIN")
        void should_RejectRegistration_when_PendingRegistrationRejected() throws Exception {
            // given
            Registration pending = registrationRepository.save(Registration.builder()
                    .username("test.ts")
                    .email("test@email.com")
                    .build());

            String jsonBody = """
                    {
                      "role": "USER",
                      "reason": "reject reason"
                    }
                    """;

            // when
            mockMvc.perform(post("/registrations/" + pending.getId())
                            .param("status", "REJECTED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isOk());

            // then
            Registration updated = registrationRepository.findById(pending.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RegistrationStatus.REJECTED);
            assertThat(updated.getReason()).isEqualTo("reject reason");

            assertThat(userRepository.findByUsername("test.ts")).isEmpty();
        }
    }
}
