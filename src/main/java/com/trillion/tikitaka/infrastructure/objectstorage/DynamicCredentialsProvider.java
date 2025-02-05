package com.trillion.tikitaka.infrastructure.objectstorage;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@RequiredArgsConstructor
public class DynamicCredentialsProvider implements AwsCredentialsProvider {

    private final CredentialScheduler credentialScheduler;

    @Override
    public AwsCredentials resolveCredentials() {
        return AwsBasicCredentials.create(
                credentialScheduler.getS3AccessKey(),
                credentialScheduler.getS3SecretKey()
        );
    }
}
