package com.trillion.tikitaka.registration.infrastructure;

import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.dto.response.RegistrationListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomRegistrationRepository {

    Page<RegistrationListResponse> getRegistrations(RegistrationStatus status, Pageable pageable);
}
