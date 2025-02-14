package com.trillion.tikitaka.registration.application;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.notification.event.RegistrationEvent;
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
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createRegistration(RegistrationRequest registrationRequest) {
        log.info("[계정 등록 요청] 아이디: {}, 이메일: {}", registrationRequest.getUsername(), registrationRequest.getEmail());
        validateDuplicateRegistration(registrationRequest.getUsername(), registrationRequest.getEmail());

        Registration registration = Registration.builder()
            .username(registrationRequest.getUsername())
            .email(registrationRequest.getEmail())
            .build();

        registrationRepository.save(registration);
    }

    private void validateDuplicateRegistration(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            log.error("[계정 등록 실패] 중복된 아이디입니다. {}", username);
            throw new DuplicatedUsernameException();
        }
        if (userRepository.existsByEmail(email)) {
            log.error("[계정 등록 실패] 중복된 이메일입니다. {}", email);
            throw new DuplicatedEmailException();
        }

        if (registrationRepository.existsByUsernameAndStatus(username, RegistrationStatus.PENDING)) {
            log.error("[계정 등록 실패] 이미 등록 신청된 아이디입니다. {}", username);
            throw new DuplicatedUsernameException();
        }
        if (registrationRepository.existsByEmailAndStatus(email, RegistrationStatus.PENDING)) {
            log.error("[계정 등록 실패] 이미 등록 신청된 이메일입니다. {}", email);
            throw new DuplicatedEmailException();
        }

        // 이미 등록 신청은 승인 되었으나 현재 사용자 존재 여부 확인
        boolean approvedRegistrationExists =
                registrationRepository.existsByUsernameAndStatus(username, RegistrationStatus.APPROVED)
                || registrationRepository.existsByEmailAndStatus(email, RegistrationStatus.APPROVED);

        if (approvedRegistrationExists) {
            boolean userExists = userRepository.existsByUsernameAndDeletedAtIsNull(username)
                    || userRepository.existsByEmailAndDeletedAtIsNull(email);

            if (userExists) {
                log.error("[계정 등록 실패] 기존 승인된 계정 등록이 존재하며, 해당 사용자가 여전히 존재합니다.");
                throw new DuplicatedUsernameException();
            }
        }
    }

    public Page<RegistrationListResponse> getRegistrations(RegistrationStatus status, Pageable pageable) {
        log.info("[계정 등록 요청 목록 조회] 조회 상태: {}", status);
        return registrationRepository.getRegistrations(status, pageable);
    }

    @Transactional
    public void processRegistration(Long registrationId, RegistrationStatus status, RegistrationProcessRequest request) {
        log.info("[계정 등록 처리] 요청 아아디: {}, {}", registrationId, status);
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(RegistrationNotFoundException::new);

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            log.error("[계정 등록 처리 실패] 이미 처리된 계정 등록 요청: {}", registrationId);
            throw new RegistrationAlreadyProcessedException();
        }

        String message = switch (status) {
            case APPROVED -> {
                log.info("[계정 등록 처리] 계정 등록 승인: {}", registrationId);
                registration.approve(request.getReason());
                yield createUser(registration.getUsername(), registration.getEmail(), request.getRole());
            }
            case REJECTED -> {
                log.info("[계정 등록 처리] 계정 등록 거부: {}", registrationId);
                registration.reject(request.getReason());
                yield request.getReason();
            }
            default -> {
                log.error("[계정 등록 처리 실패] 유효하지 않은 상태: {}", status);
                throw new CustomException(ErrorCode.INVALID_REQUEST_VALUE);
            }
        };

        publishRegistrationEvent(registration, message, request.getRole());
    }

    private void publishRegistrationEvent(Registration registration, String message, Role role) {
        eventPublisher.publishEvent(new RegistrationEvent(
                this,
                registration.getUsername(),
                registration.getEmail(),
                message,
                role,
                registration.getStatus(),
                NotificationType.USER_REGISTRATION
        ));
    }

    private String createUser(String username, String email, Role role) {
        log.info("[계정 생성] 아이디: {}, 이메일: {}", username, email);
        String rawPassword = PasswordGenerator.generateRandomPassword();

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        userRepository.save(user);

        return rawPassword;
    }
}
