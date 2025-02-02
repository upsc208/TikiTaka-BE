/*package com.trillion.tikitaka.user;

import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(1L)
                .username("admin.tk")
                .email("admin@dktechin.co.kr")
                .password("$2a$10$XCXiVezs0l4YNXU0iwA5qO1y79ETEkqx5moWhZjkUPgwwRilxvWf.")
                .role("ADMIN")
                .deletedAt(null)
                .locked(false)
                .loginFailCount(0)
                .lastLoginAt(LocalDateTime.now())
                .lastPasswordChangedAt(LocalDateTime.now())
                .lockExpireAt(null)
                .build();
    }

    @Test
    void 소프트_삭제_성공() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.softDeleteUser(1L);

        assertNotNull(user.getDeletedAt()); // ✅ 삭제 시간이 설정되었는지 확인
    }

    @Test
    void 사용자_없을_때_예외발생() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.softDeleteUser(1L));
    }
}
*/