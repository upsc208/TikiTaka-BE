package com.trillion.tikitaka.user.infrastructure;

import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByRole(Role role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
