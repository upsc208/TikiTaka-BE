/*
package com.trillion.tikitaka;


import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class makeUser implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("Password1234!");

        User user1 = User.builder()
                .username("user.tk")
                .password(encodedPassword)
                .email("user@dktechin.co.kr")
                .role(Role.USER)
                .build();

        userRepository.save(user1);
    }
}

*/
