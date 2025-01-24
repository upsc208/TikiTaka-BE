package com.trillion.tikitaka.registration.application;

import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.domain.Registration;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
import com.trillion.tikitaka.registration.exception.DuplicatedEmailException;
import com.trillion.tikitaka.registration.exception.DuplicatedUsernameException;
import com.trillion.tikitaka.registration.exception.RegistrationAlreadyProcessedException;
import com.trillion.tikitaka.registration.exception.RegistrationNotFoundException;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

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
    public void processRegistration(Long registrationId, RegistrationStatus status) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(RegistrationNotFoundException::new);

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RegistrationAlreadyProcessedException();
        }

        switch (status) {
            case APPROVED -> registration.approve();
            case REJECTED -> registration.reject();
            default -> throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_VALUE.getMessage());
        }

        // TODO: 카카오워크로 랜덤 비밀번호 생성 후 알림 전송 추가
    }
}
