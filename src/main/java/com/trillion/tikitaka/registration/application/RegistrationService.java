package com.trillion.tikitaka.registration.application;

import com.trillion.tikitaka.registration.domain.Registration;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.request.RegistrationRequest;
import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
import com.trillion.tikitaka.registration.exception.DuplicateEmailException;
import com.trillion.tikitaka.registration.exception.DuplicateUsernameException;
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
                throw new DuplicateUsernameException();
            } else if (registration.getEmail().equals(registrationRequest.getEmail())) {
                throw new DuplicateEmailException();
            }
        });

        Registration registration = Registration.builder()
            .username(registrationRequest.getUsername())
            .email(registrationRequest.getEmail())
            .build();

        registrationRepository.save(registration);
    }

    // 계정 등록 목록 조회
    public Page<RegistrationListResponse> getRegistrations(RegistrationStatus status, Pageable pageable) {
        return registrationRepository.getRegistrations(status, pageable);
    }
}
