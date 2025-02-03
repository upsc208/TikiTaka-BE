package com.trillion.tikitaka.registration.infrastructure;

import com.trillion.tikitaka.registration.domain.Registration;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, CustomRegistrationRepository {
    boolean existsByUsernameAndStatusNot(String username, RegistrationStatus status);
    boolean existsByEmailAndStatusNot(String email, RegistrationStatus status);
    Long countByStatus(RegistrationStatus status);
}
