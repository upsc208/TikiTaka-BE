package com.trillion.tikitaka.authentication.application.handler;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private final UserRepository userRepository;

    @Override
    @Transactional
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        log.error("[인증] 추가 인증 체크");
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();

        if (user.isLocked()) {
            if (user.getLockExpireAt() != null && LocalDateTime.now().isBefore(user.getLockExpireAt())) {
                long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockExpireAt());
                log.error("[인증] 계정이 잠겼습니다. 남은 시간: {}분", minutesLeft);
                throw new LockedException("남은 시간:" + minutesLeft + "분");
            } else {
                user.resetLoginFailCount();
                userRepository.saveAndFlush(user);
            }
        }

        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
        } catch (BadCredentialsException ex) {
            log.error("[인증] 로그인 실패");
            user.handleLoginFailure();
            userRepository.save(user);
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        user.resetLoginFailCount();
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("[인증] 로그인 성공");
    }
}
