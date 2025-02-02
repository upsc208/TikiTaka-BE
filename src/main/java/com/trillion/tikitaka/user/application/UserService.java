package com.trillion.tikitaka.user.application;

import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.PasswordChangeRequest;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 비밀번호 변경 로직
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        String rawPassword = "Password123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 새 비밀번호 유효성 검사 (8자 이상, 알파벳, 숫자, 특수문자 포함)
        if (!isValidPassword(request.getNewPassword())) {
            throw new WeakPasswordException();
        }

        // 현재 비밀번호와 새 비밀번호가 같으면 예외 발생
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        // 새 비밀번호를 해싱 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }

    // 비밀번호 유효성 검사 메서드
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$");
    }
}
