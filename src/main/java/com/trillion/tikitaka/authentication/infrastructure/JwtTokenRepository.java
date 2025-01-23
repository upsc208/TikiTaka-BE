package com.trillion.tikitaka.authentication.infrastructure;

import com.trillion.tikitaka.authentication.domain.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
}
