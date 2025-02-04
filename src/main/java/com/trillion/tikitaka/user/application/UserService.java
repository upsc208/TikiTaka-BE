package com.trillion.tikitaka.user.application;

import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.dto.PasswordChangeRequest;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.exception.InvalidPasswordException;
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

    public void updatePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
