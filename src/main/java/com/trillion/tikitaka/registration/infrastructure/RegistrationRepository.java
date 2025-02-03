package com.trillion.tikitaka.registration.infrastructure;

import com.trillion.tikitaka.registration.domain.Registration;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, CustomRegistrationRepository {
    Optional<Registration> findByUsernameOrEmail(String username, String email);
    Long countByStatus(RegistrationStatus status);
}
