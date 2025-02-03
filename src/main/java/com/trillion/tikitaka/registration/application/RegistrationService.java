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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        registrationRepository.findByUsernameOrEmail(
                registrationRequest.getUsername(),
                registrationRequest.getEmail()
        ).ifPresent((registration) -> {
            if (registration.getUsername().equals(registrationRequest.getUsername())) {
                throw new DuplicatedUsernameException();
            } else if (registration.getEmail().equals(registrationRequest.getEmail())) {
                throw new DuplicatedEmailException();
            }
        });

        Registration registration = Registration.builder()
            .username(registrationRequest.getUsername())
            .email(registrationRequest.getEmail())
            .build();

        registrationRepository.save(registration);
    }

    public Page<RegistrationListResponse> getRegistrations(RegistrationStatus status, Pageable pageable) {
        return registrationRepository.getRegistrations(status, pageable);
    }

    @Transactional
    public void processRegistration(Long registrationId, RegistrationStatus status, RegistrationProcessRequest request) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(RegistrationNotFoundException::new);

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RegistrationAlreadyProcessedException();
        }

        Role role = validateRole(request.getRole());

        String message = switch (status) {
            case APPROVED -> {
                registration.approve(request.getReason());
                yield createUser(registration.getUsername(), registration.getEmail(), role);
            }
            case REJECTED -> {
                registration.reject(request.getReason());
                yield request.getReason();
            }
            default -> throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_VALUE.getMessage());
        };

        publishRegistrationEvent(registration, message);
    }

    private Role validateRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (CustomException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_VALUE);
        }
    }

    private void publishRegistrationEvent(Registration registration, String message) {
        eventPublisher.publishEvent(new RegistrationEvent(
                this,
                registration.getUsername(),
                registration.getEmail(),
                message,
                registration.getStatus(),
                NotificationType.USER_REGISTRATION
        ));
    }

    private String createUser(String username, String email, Role role) {
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
