package com.trillion.tikitaka.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter;
import com.trillion.tikitaka.authentication.application.handler.CustomAuthenticationFailureHandler;
import com.trillion.tikitaka.authentication.application.handler.CustomAuthenticationSuccessHandler;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/registrations", "/reissue").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterAfter(customAuthenticationFilter(), LogoutFilter.class)

                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(objectMapper);
        filter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(customAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(jwtUtil);
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
