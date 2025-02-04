package com.trillion.tikitaka.global.config;

import com.trillion.tikitaka.infrastructure.objectstorage.CredentialScheduler;
import com.trillion.tikitaka.infrastructure.objectstorage.DynamicCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class ObjectStorageConfig {

    @Value("${kakaocloud.object-storage.endpoint}")
    private String endpoint;

    @Value("${kakaocloud.object-storage.region}")
    private String region;

    @Bean
    public S3Client s3Client(@Lazy CredentialScheduler credentialScheduler) {
        DynamicCredentialsProvider credentialsProvider = new DynamicCredentialsProvider(credentialScheduler);
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .region(Region.of(region))
                .build();
    }
}
