package com.trillion.tikitaka.user.infrastructure;

import com.trillion.tikitaka.user.domain.UserPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, Long> {
}
