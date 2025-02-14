package com.trillion.tikitaka.user.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.request.PasswordChangeRequest;
import com.trillion.tikitaka.user.dto.response.RegistrationAndUserCountResponse;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TicketRepository ticketRepository;

    @Transactional
    public void updatePassword(Long userId, PasswordChangeRequest request) {
        log.info("[비밀번호 변경] 사용자 ID: {}", userId);
        if(request.getCurrentPassword().equals(request.getNewPassword())) {
            log.error("[비밀번호 변경] 새 비밀번호가 기존 비밀번호와 동일");
            throw new CustomException(ErrorCode.NEW_PASSWORD_NOT_CHANGED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.error("[비밀번호 변경] 현재 비밀번호 불일치");
            throw new CustomException(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, CustomUserDetails userDetails) {
        log.info("[사용자 삭제] 사용자 ID: {}", userId);

        if (userId.equals(userDetails.getId())) {
            log.error("[사용자 삭제] 자기 자신 삭제 불가");
            throw new CustomException(ErrorCode.CANNOT_DELETE_MYSELF);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        ticketRepository.softDeleteTicketsByRequester(userId);
        userRepository.delete(user);
    }

    public RegistrationAndUserCountResponse getRegistrationAndUserCount() {
        log.info("[회원가입 및 사용자 수 조회]");
        return new RegistrationAndUserCountResponse(
                registrationRepository.countByStatus(RegistrationStatus.PENDING),
                userRepository.count()
        );
    }

    public UserListResponse getUserListResponse(Role role, CustomUserDetails userDetails) {
        log.info("[사용자 목록 조회] 권한: {}", role);
        Role currentUserRole = userDetails.getUser().getRole();
        if ((currentUserRole == Role.USER || currentUserRole == Role.MANAGER) && role == Role.ADMIN) {
            log.error("[사용자 목록 조회] 권한 없음");
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return userRepository.getAllUsersByRole(role, currentUserRole);
    }

    public UserResponse getUserResponse(Long userId) {
        log.info("[사용자 조회] 사용자 ID: {}", userId);
        UserResponse userResponse = userRepository.getUserResponse(userId);
        if (userResponse == null) {
            log.error("[사용자 조회] 사용자 없음");
            throw new UserNotFoundException();
        }
        return userResponse;
    }

    public UserResponse getMyUserResponse(CustomUserDetails userDetails) {
        log.info("[내 정보 조회]");
        UserResponse userResponse = userRepository.getUserResponse(userDetails.getId());
        if (userResponse == null) {
            log.error("[내 정보 조회] 사용자 없음");
            throw new UserNotFoundException();
        }
        return userResponse;
    }

    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        log.info("[사용자 권한 변경] 사용자 ID: {}, 새 권한: {}", userId, newRole);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateRole(newRole);
        userRepository.save(user);
    }
}