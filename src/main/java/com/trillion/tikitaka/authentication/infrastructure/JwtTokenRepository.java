package com.trillion.tikitaka.authentication.infrastructure;

import com.trillion.tikitaka.authentication.domain.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Boolean existsByRefreshToken(String username);

    @Transactional
    void deleteByRefreshToken(String refreshToken);
}
