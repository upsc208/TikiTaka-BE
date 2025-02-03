package com.trillion.tikitaka.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class KakaoWorkConfig {

    @Value("${kakaowork.api.base-url}")
    private String baseUrl;

    @Value("${kakaowork.api.app-key}")
    private String appKey;

    private final WebClient.Builder webClientBuilder;

    @Bean(name = "kakaoWorkWebClient")
    public WebClient kakaoWorkWebClient() {
        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + appKey)
                .build();
    }
}
