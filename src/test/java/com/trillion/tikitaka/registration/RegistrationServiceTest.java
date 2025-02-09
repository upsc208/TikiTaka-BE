//package com.trillion.tikitaka.registration;
//
//import com.trillion.tikitaka.registration.application.RegistrationService;
//import com.trillion.tikitaka.registration.domain.Registration;
//import com.trillion.tikitaka.registration.domain.RegistrationStatus;
//import com.trillion.tikitaka.registration.dto.request.RegistrationProcessRequest;
//import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
//import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
//import com.trillion.tikitaka.registration.exception.DuplicatedEmailException;
//import com.trillion.tikitaka.registration.exception.DuplicatedUsernameException;
//import com.trillion.tikitaka.registration.exception.RegistrationAlreadyProcessedException;
//import com.trillion.tikitaka.registration.exception.RegistrationNotFoundException;
//import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
//import com.trillion.tikitaka.user.infrastructure.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.Mockito.*;
//
//@DisplayName("계정 등록 유닛 테스트")
//class RegistrationServiceTest {
//
//    @Mock
//    private RegistrationRepository registrationRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    private RegistrationService registrationService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        registrationService = new RegistrationService(registrationRepository, userRepository, passwordEncoder, eventPublisher);
//    }
//
//    @Nested
//    @DisplayName("계정 등록 신청 테스트")
//    class DescribeCreateRegistration {
//
//        @Test
//        @DisplayName("계정 등록 요청한 아이디를 사용하는 사용자가 존재하면 오류가 발생한다.")
//        void should_FailToCreateRegistration_when_DuplicateUsernameFromUser() {
//            // given
//            Registration existing = Registration.builder()
//                    .username("duplicate.test")
//                    .email("test@email.com")
//                    .build();
//
//            RegistrationRequest request = new RegistrationRequest("duplicate.test", "test@email.com");
//
//            when(userRepository.existsByUsername(existing.getUsername())).thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.createRegistration(request))
//                    .isInstanceOf(DuplicatedUsernameException.class);
//        }
//
//        @Test
//        @DisplayName("계정 등록 요청한 아이디가 이미 존재하면 오류가 발생한다.")
//        void should_FailToCreateRegistration_when_DuplicateUsernameFromRegistration() {
//            // given
//            Registration existing = Registration.builder()
//                    .username("duplicate.test")
//                    .email("test@email.com")
//                    .build();
//
//            RegistrationRequest request = new RegistrationRequest("duplicate.test", "test@email.com");
//
//            when(registrationRepository.existsByUsernameAndStatusNot(existing.getUsername(), RegistrationStatus.REJECTED))
//                    .thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.createRegistration(request))
//                    .isInstanceOf(DuplicatedUsernameException.class);
//        }
//
//        @Test
//        @DisplayName("계정 등록 요청한 이메을 사용하는 사용자가 존재하면 오류가 발생한다.")
//        void should_FailToCreateRegistration_when_DuplicateEmailForUser() {
//            /// given
//            Registration existing = Registration.builder()
//                    .username("user.test")
//                    .email("duplicate@email.com")
//                    .build();
//
//            RegistrationRequest request = new RegistrationRequest("unique.test", "duplicate@email.com");
//
//            when(userRepository.existsByEmail(existing.getEmail()))
//                    .thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.createRegistration(request))
//                    .isInstanceOf(DuplicatedEmailException.class);
//        }
//
//        @Test
//        @DisplayName("계정 등록 요청한 이메일이 이미 존재하면 오류가 발생한다.")
//        void should_FailToCreateRegistration_when_DuplicateEmailForRegistration() {
//            /// given
//            Registration existing = Registration.builder()
//                    .username("user.test")
//                    .email("duplicate@email.com")
//                    .build();
//
//            RegistrationRequest request = new RegistrationRequest("unique.test", "duplicate@email.com");
//
//            when(registrationRepository.existsByEmailAndStatusNot(existing.getEmail(), RegistrationStatus.REJECTED))
//                    .thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.createRegistration(request))
//                    .isInstanceOf(DuplicatedEmailException.class);
//        }
//
//        @Test
//        @DisplayName("계정 등록 요청한 아이디와 이메일이 중복되지 않고 양식에 맞으면 계정 등록이 성공한다.")
//        void should_CreateRegistration_when_ValidRequest() {
//            // given
//            RegistrationRequest request = new RegistrationRequest("unique.test", "unique@email.com");
//
//            when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
//            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//            when(registrationRepository.existsByUsernameAndStatusNot(request.getUsername(), RegistrationStatus.REJECTED))
//                    .thenReturn(false);
//            when(registrationRepository.existsByEmailAndStatusNot(request.getEmail(), RegistrationStatus.REJECTED))
//                    .thenReturn(false);
//
//            // when
//            registrationService.createRegistration(request);
//
//            // then
//            verify(registrationRepository, times(1))
//                    .save(argThat(reg -> reg.getUsername().equals("unique.test")
//                            && reg.getEmail().equals("unique@email.com")
//                            && reg.getStatus() == RegistrationStatus.PENDING));
//        }
//    }
//
//    @Nested
//    @DisplayName("계정 등록 조회 테스트")
//    class DescribeGetRegistrations {
//
//        @Test
//        @DisplayName("주어진 상태와 페이징 정보를 이용해 해당 페이지의 등록 목록을 반환한다.")
//        void should_ReturnRegistrations_when_ValidRequest() {
//            // given
//            RegistrationStatus status = RegistrationStatus.PENDING;
//            Pageable pageable = PageRequest.of(0, 10);
//
//            List<RegistrationListResponse> content = List.of(
//                    new RegistrationListResponse(1L, "user1", "user1@email",
//                            RegistrationStatus.PENDING, null),
//                    new RegistrationListResponse(2L, "user2", "user2@email",
//                            RegistrationStatus.PENDING, null)
//            );
//
//            Page<RegistrationListResponse> mockPage =
//                    new PageImpl<>(content, pageable, 2);
//
//            when(registrationRepository.getRegistrations(status, pageable))
//                    .thenReturn(mockPage);
//
//            // when
//            Page<RegistrationListResponse> result = registrationService.getRegistrations(status, pageable);
//
//            // then
//            assertThat(result.getTotalElements()).isEqualTo(2);
//            assertThat(result.getContent().get(0).getUsername()).isEqualTo("user1");
//            assertThat(result.getContent().get(1).getUsername()).isEqualTo("user2");
//        }
//
//        @Test
//        @DisplayName("주어진 상태가 없을 때 페이징 정보를 이용해 해당 페이지의 모든 계정 등록 요청 목록을 반환한다.")
//        void should_ReturnAllRegistrations_when_ValidRequestWithNoStatus() {
//            // given
//            RegistrationStatus status = null;
//            Pageable pageable = PageRequest.of(0, 10);
//
//            List<RegistrationListResponse> content = List.of(
//                    new RegistrationListResponse(1L, "user1", "user1@email",
//                            RegistrationStatus.PENDING, null),
//                    new RegistrationListResponse(2L, "user2", "user2@email",
//                            RegistrationStatus.REJECTED, null),
//                    new RegistrationListResponse(3L, "user3", "user3@email",
//                            RegistrationStatus.APPROVED, null)
//            );
//
//            Page<RegistrationListResponse> mockPage =
//                    new PageImpl<>(content, pageable, 3);
//
//            when(registrationRepository.getRegistrations(status, pageable))
//                    .thenReturn(mockPage);
//
//            // when
//            Page<RegistrationListResponse> result = registrationService.getRegistrations(status, pageable);
//
//            // then
//            assertThat(result.getTotalElements()).isEqualTo(3);
//            assertThat(result.getContent().get(0).getUsername()).isEqualTo("user1");
//            assertThat(result.getContent().get(1).getUsername()).isEqualTo("user2");
//            assertThat(result.getContent().get(2).getUsername()).isEqualTo("user3");
//        }
//    }
//
//    @Nested
//    @DisplayName("계정 등록 처리 테스트")
//    class DescribeProcessRegistration {
//
//        @Test
//        @DisplayName("유효하지 않은 ROLE이 들어오면 예외가 발생한다.")
//        void should_FailToProcessRegistration_when_RoleIsInvalid() {
//            // given
//            Registration pending = Registration.builder()
//                    .username("pendingUser")
//                    .email("pending@user")
//                    .build();
//            ReflectionTestUtils.setField(pending, "id", 1L);
//            ReflectionTestUtils.setField(pending, "status", RegistrationStatus.PENDING);
//            when(registrationRepository.findById(1L)).thenReturn(Optional.of(pending));
//
//            RegistrationProcessRequest request = new RegistrationProcessRequest("WRONG_ROLE", "approve reason");
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.processRegistration(1L, RegistrationStatus.APPROVED, request))
//                    .isInstanceOf(IllegalArgumentException.class);
//        }
//
//        @Test
//        @DisplayName("주어진 등록 ID에 해당하는 등록 정보가 없을 때 오류가 발생한다.")
//        void should_FailToProcessRegistration_when_RegistrationNotFound() {
//            // given
//            Long notExistingId = 999L;
//            when(registrationRepository.findById(notExistingId))
//                    .thenReturn(Optional.empty());
//
//            RegistrationProcessRequest request = new RegistrationProcessRequest("USER", "error reason");
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.processRegistration(notExistingId, RegistrationStatus.APPROVED, request))
//                    .isInstanceOf(RegistrationNotFoundException.class);
//        }
//
//        @Test
//        @DisplayName("대기 중인 등록 정보가 아닌 경우 처리할 수 없다.")
//        void should_FailToProcessRegistration_when_RegistrationStatusIsNotPending() {
//            // given
//            Registration existing = Registration.builder()
//                    .username("user.test")
//                    .email("user@email.com")
//                    .build();
//            existing.approve("approve reason");
//
//            RegistrationProcessRequest request = new RegistrationProcessRequest("USER", "approve reason");
//
//            when(registrationRepository.findById(1L))
//                    .thenReturn(Optional.of(existing));
//
//            // when & then
//            assertThatThrownBy(() -> registrationService.processRegistration(1L, RegistrationStatus.APPROVED, request))
//                    .isInstanceOf(RegistrationAlreadyProcessedException.class);
//        }
//
//        @Test
//        @DisplayName("대기 중인 등록 정보를 승인하면 상태가 승인으로 변경된다.")
//        void should_ApproveRegistration_when_ApprovePendingRegistration() {
//            // given
//            Registration pending = Registration.builder()
//                    .username("pendingUser")
//                    .email("pending@user")
//                    .build();
//
//            RegistrationProcessRequest request = new RegistrationProcessRequest("USER", "approve reason");
//
//            when(registrationRepository.findById(1L))
//                    .thenReturn(Optional.of(pending));
//
//            // when
//            registrationService.processRegistration(1L, RegistrationStatus.APPROVED, request);
//
//            // then
//            assertThat(pending.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
//        }
//
//        @Test
//        @DisplayName("대기 중인 등록 정보를 거부하면 상태가 거부로 변경된다.")
//        void should_RejectRegistration_when_RejectPendingRegistration() {
//            // given
//            Registration pending = Registration.builder()
//                    .username("pendingUser")
//                    .email("pending@user")
//                    .build();
//
//            RegistrationProcessRequest request = new RegistrationProcessRequest("USER", "reject reason");
//
//            when(registrationRepository.findById(1L))
//                    .thenReturn(Optional.of(pending));
//
//            // when
//            registrationService.processRegistration(1L, RegistrationStatus.REJECTED, request);
//
//            // then
//            assertThat(pending.getStatus()).isEqualTo(RegistrationStatus.REJECTED);
//        }
//    }
//}