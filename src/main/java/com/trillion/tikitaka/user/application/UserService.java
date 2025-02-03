package com.trillion.tikitaka.user.application;

import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

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
        return userRepository.getUserResponse(userId);
    }
}
