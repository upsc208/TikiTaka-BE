package com.trillion.tikitaka.user.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.request.PasswordChangeRequest;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.trillion.tikitaka.user.domain.Role;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updatePassword(Long userId, PasswordChangeRequest request) {
        if(request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_NOT_CHANGED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);
    }

    public RegistrationAndUserCountResponse getRegistrationAndUserCount() {
        return new RegistrationAndUserCountResponse(
                registrationRepository.countByStatus(RegistrationStatus.PENDING),
                userRepository.count()
        );
    }

    public UserListResponse findAllUsers() {
        return userRepository.findAllUser();
    }

    public UserResponse getUserResponse(Long userId) {
        UserResponse userResponse = userRepository.getUserResponse(userId);
        if (userResponse == null) throw new UserNotFoundException();
        return userResponse;
    }

    public UserResponse getMyUserResponse(CustomUserDetails userDetails) {
        UserResponse userResponse = userRepository.getUserResponse(userDetails.getId());
        if (userResponse == null) throw new UserNotFoundException();
        return userResponse;
    }

    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateRole(newRole);
        userRepository.save(user);
    }
}