package com.trillion.tikitaka.user.application;

import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.PasswordChangeRequest;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (!isValidPassword(request.getNewPassword())) {
            throw new WeakPasswordException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        if (user.isSameAsPreviousPassword(passwordEncoder.encode(request.getNewPassword()))) {
            throw new SamePasswordException();
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        user.updatePasswordHistory(user.getPassword());
        userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$");
    }
}
