package com.trillion.tikitaka.registration;

import com.trillion.tikitaka.registration.application.RegistrationService;
import com.trillion.tikitaka.registration.domain.Registration;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.request.RegistrationProcessRequest;
import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
import com.trillion.tikitaka.registration.exception.DuplicatedEmailException;
import com.trillion.tikitaka.registration.exception.DuplicatedUsernameException;
import com.trillion.tikitaka.registration.exception.RegistrationAlreadyProcessedException;
import com.trillion.tikitaka.registration.exception.RegistrationNotFoundException;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@DisplayName("계정 등록 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RegistrationService registrationService;

    @Nested
    @DisplayName("계정 등록 생성 테스트")
    class DescribeCreateRegistration {

        private Validator validator;

        @BeforeEach
        void setUp() {
            validator = Validation.buildDefaultValidatorFactory().getValidator();
        }

        @Test
        @DisplayName("계정 등록 요청한 아이디로 이미 등록 요청이 존재하면 오류가 발생한다.")
        void should_FailToCreateRegistration_when_DuplicateUsernameFromRegistration() {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "test@email.com");

            when(registrationRepository.existsByUsernameAndStatus(request.getUsername(), RegistrationStatus.PENDING))
                    .thenReturn(true); // 올바른 조건 설정

            // when & then
            assertThatThrownBy(() -> registrationService.createRegistration(request))
                    .isInstanceOf(DuplicatedUsernameException.class)
                    .hasMessage("이미 사용중인 아이디입니다.");
        }


        @Test
        @DisplayName("계정 등록 요청한 이메일로 이미 등록 요청이 존재하면 오류가 발생한다.")
        void should_FailToCreateRegistration_when_DuplicateEmailFromRegistration() {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "test@email.com");

            when(registrationRepository.existsByEmailAndStatus(request.getEmail(), RegistrationStatus.PENDING))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> registrationService.createRegistration(request))
                    .isInstanceOf(DuplicatedEmailException.class)
                    .hasMessage("이미 사용중인 이메일입니다.");
        }

        @Test
        @DisplayName("계정 등록 요청한 아이디로 이미 사용자가 존재하면 오류가 발생한다.")
        void should_FailToCreateRegistration_when_DuplicateUsernameFromUser() {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "test@email.com");
            when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> registrationService.createRegistration(request))
                    .isInstanceOf(DuplicatedUsernameException.class);
        }

        @Test
        @DisplayName("계정 등록 요청한 이메일로 이미 사용자가 존재하면 오류가 발생한다.")
        void should_FailToCreateRegistration_when_DuplicateEmailFromUser() {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "test@email.com");
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> registrationService.createRegistration(request))
                    .isInstanceOf(DuplicatedEmailException.class);
        }

        @Test
        @DisplayName("계정 등록 요청한 아이디 양식이 맞지 않으면 오류가 발생한다.")
        void should_FailValidation_when_UsernameDoesNotMatchPattern() {
            // given
            RegistrationRequest request = new RegistrationRequest("InvalidUsername", "test@example.com");

            // when
            Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
            ConstraintViolation<RegistrationRequest> violation = violations.iterator().next();

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violation.getMessage())
                    .contains("아이디는 'aaa.bbb' 형식으로 3~10자의 소문자 + '.' + 1~5자의 소문자로 입력하세요.");
        }

        @Test
        @DisplayName("계정 등록 요청한 이메일 양식이 맞지 않으면 오류가 발생한다.")
        void should_FailValidation_when_EmailDoesNotMatchPattern() {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "invalidemail");

            // when
            Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
            ConstraintViolation<RegistrationRequest> violation = violations.iterator().next();

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("계정 등록 요청한 아이디와 이메일이 중복되지 않고 양식에 맞으면 계정 등록이 성공한다.")
        void should_CreateRegistration_when_ValidRequest() {
            // given
            RegistrationRequest request = new RegistrationRequest("test.ts", "test@email.com");

            when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

            // when
            registrationService.createRegistration(request);

            // then
            verify(registrationRepository, times(1))
                    .save(argThat(reg -> reg.getUsername().equals("test.ts")
                            && reg.getEmail().equals("test@email.com")
                            && reg.getStatus() == RegistrationStatus.PENDING));
        }

    }

    @Nested
    @DisplayName("계정 등록 조회 테스트")
    class DescribeGetRegistrations {

        @Test
        @DisplayName("주어진 상태와 페이징 정보를 이용해 해당 페이지의 등록 목록을 반환한다.")
        void should_ReturnRegistrations_when_ValidRequest() {
            // given
            RegistrationStatus status = RegistrationStatus.PENDING;
            Pageable pageable = PageRequest.of(0, 10);

            List<RegistrationListResponse> content = List.of(
                    new RegistrationListResponse(1L, "test.ts", "test@email", status, null),
                    new RegistrationListResponse(2L, "tester.ts", "tester@email", status, null)
            );

            Page<RegistrationListResponse> mockPage = new PageImpl<>(content, pageable, 2);
            when(registrationRepository.getRegistrations(status, pageable)).thenReturn(mockPage);

            // when
            Page<RegistrationListResponse> result = registrationService.getRegistrations(status, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getUsername()).isEqualTo("test.ts");
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@email");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(status);
            assertThat(result.getContent().get(1).getUsername()).isEqualTo("tester.ts");
            assertThat(result.getContent().get(1).getEmail()).isEqualTo("tester@email");
            assertThat(result.getContent().get(1).getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("주어진 상태가 없을 때 페이징 정보를 이용해 해당 페이지의 모든 계정 등록 요청 목록을 반환한다.")
        void should_ReturnAllRegistrations_when_ValidRequestWithNoStatus() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            List<RegistrationListResponse> content = List.of(
                    new RegistrationListResponse(1L, "test.ts", "test@email",
                            RegistrationStatus.PENDING, null),
                    new RegistrationListResponse(2L, "tester.ts", "tester@email",
                            RegistrationStatus.REJECTED, null),
                    new RegistrationListResponse(3L, "tests.ts", "tests@email",
                            RegistrationStatus.APPROVED, null)
            );

            Page<RegistrationListResponse> mockPage = new PageImpl<>(content, pageable, 3);
            when(registrationRepository.getRegistrations(null, pageable)).thenReturn(mockPage);

            // when
            Page<RegistrationListResponse> result = registrationService.getRegistrations(null, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent().get(0).getUsername()).isEqualTo("test.ts");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(RegistrationStatus.PENDING);
            assertThat(result.getContent().get(1).getUsername()).isEqualTo("tester.ts");
            assertThat(result.getContent().get(1).getStatus()).isEqualTo(RegistrationStatus.REJECTED);
            assertThat(result.getContent().get(2).getUsername()).isEqualTo("tests.ts");
            assertThat(result.getContent().get(2).getStatus()).isEqualTo(RegistrationStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("계정 등록 처리 테스트")
    class DescribeProcessRegistration {

        @Test
        @DisplayName("주어진 등록 ID에 해당하는 등록 정보가 없을 때 오류가 발생한다.")
        void should_FailToProcessRegistration_when_RegistrationNotFound() {
            // given
            Long notExistingId = 999L;
            when(registrationRepository.findById(notExistingId))
                    .thenReturn(Optional.empty());

            RegistrationProcessRequest request = new RegistrationProcessRequest(Role.USER, "error reason");

            // when & then
            assertThatThrownBy(() -> registrationService.processRegistration(notExistingId, RegistrationStatus.APPROVED, request))
                    .isInstanceOf(RegistrationNotFoundException.class);
        }

        @Test
        @DisplayName("대기 중인 등록 정보가 아닌 경우 처리 오류가 발생한다.")
        void should_FailToProcessRegistration_when_RegistrationStatusIsNotPending() {
            // given
            Registration existing = Registration.builder()
                    .username("test.ts")
                    .email("user@email.com")
                    .build();
            existing.approve("approve reason");

            RegistrationProcessRequest request = new RegistrationProcessRequest(Role.USER, "approve reason");

            when(registrationRepository.findById(1L)).thenReturn(Optional.of(existing));

            // when & then
            assertThatThrownBy(() -> registrationService.processRegistration(1L, RegistrationStatus.APPROVED, request))
                    .isInstanceOf(RegistrationAlreadyProcessedException.class);
        }

        @Test
        @DisplayName("대기 중인 등록 정보를 승인하면 상태가 승인으로 변경된다.")
        void should_ApproveRegistration_when_ApprovePendingRegistration() {
            // given
            Registration pending = Registration.builder()
                    .username("test.ts")
                    .email("test@email.com")
                    .build();

            RegistrationProcessRequest request = new RegistrationProcessRequest(Role.USER, "approve reason");

            when(registrationRepository.findById(1L))
                    .thenReturn(Optional.of(pending));

            // when
            registrationService.processRegistration(1L, RegistrationStatus.APPROVED, request);

            // then
            assertThat(pending.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
        }

        @Test
        @DisplayName("대기 중인 등록 정보를 거부하면 상태가 거부로 변경된다.")
        void should_RejectRegistration_when_RejectPendingRegistration() {
            // given
            Registration pending = Registration.builder()
                    .username("test.ts")
                    .email("test@email.com")
                    .build();

            RegistrationProcessRequest request = new RegistrationProcessRequest(Role.USER, "reject reason");

            when(registrationRepository.findById(1L))
                    .thenReturn(Optional.of(pending));

            // when
            registrationService.processRegistration(1L, RegistrationStatus.REJECTED, request);

            // then
            assertThat(pending.getStatus()).isEqualTo(RegistrationStatus.REJECTED);
        }
    }
}