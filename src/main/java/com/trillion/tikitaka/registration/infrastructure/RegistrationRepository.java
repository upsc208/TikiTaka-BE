package com.trillion.tikitaka.registration.infrastructure;

import com.trillion.tikitaka.registration.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUsernameOrEmail(String username, String email);
}
