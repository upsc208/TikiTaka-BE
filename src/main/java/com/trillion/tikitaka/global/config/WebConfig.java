package com.trillion.tikitaka.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("dev")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://210.109.54.71:8080", "http://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
