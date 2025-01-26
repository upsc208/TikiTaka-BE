package com.trillion.tikitaka.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trillion.tikitaka.authentication.application.CustomUserDetailsService;
import com.trillion.tikitaka.authentication.application.filter.CustomAuthenticationFilter;
import com.trillion.tikitaka.authentication.application.filter.CustomLogoutFilter;
import com.trillion.tikitaka.authentication.application.filter.JwtFilter;
import com.trillion.tikitaka.authentication.application.handler.*;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.infrastructure.JwtTokenRepository;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtTokenRepository jwtTokenRepository;

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

                .exceptionHandling((exception) -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .addFilterAt(customLogoutFilter(), LogoutFilter.class)
                .addFilterAfter(customAuthenticationFilter(), LogoutFilter.class)
                .addFilterBefore(jwtFilter(), CustomAuthenticationFilter.class)

                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(customAuthenticationProvider());
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(objectMapper);
        filter.setAuthenticationManager(authenticationManager());
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
    public CustomAuthenticationProvider customAuthenticationProvider() {
        CustomAuthenticationProvider provider = new CustomAuthenticationProvider(userRepository);
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil, objectMapper);
    }

    @Bean
    public CustomLogoutFilter customLogoutFilter() {
        return new CustomLogoutFilter(jwtUtil, jwtTokenRepository);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
