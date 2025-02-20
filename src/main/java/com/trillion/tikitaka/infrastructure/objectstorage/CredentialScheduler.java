package com.trillion.tikitaka.infrastructure.objectstorage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@Service
public class CredentialScheduler {

    @Value("${kakaocloud.object-storage.iam.access-key}")
    private String iamAccessKey;

    @Value("${kakaocloud.object-storage.iam.secret-key}")
    private String iamSecretKey;

    @Value("${kakaocloud.object-storage.iam.user-id}")
    private String iamUserId;

    @Value("${kakaocloud.object-storage.iam.project-id}")
    private String iamProjectId;

    private final WebClient webClient;

    private volatile String apiToken;
    private volatile String s3AccessKey;
    private volatile String s3SecretKey;

    public CredentialScheduler(WebClient webClient) {
        this.webClient = webClient;
    }

    // 6시간마다 실행되어 최신 API 토큰 및 S3 크리덴셜을 갱신 (6 * 60 * 60 * 1000 밀리초 간격)
    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 6 * 60 * 60 * 1000)
    public void refreshCredentials() {
        log.info("자격증명 갱신 시작...");
        try {
            String newApiToken = fetchApiToken();

            Map<String, String> credentials = fetchS3Credentials(newApiToken);
            if (newApiToken != null && credentials != null) {
                this.apiToken = newApiToken;
                this.s3AccessKey = credentials.get("access");
                this.s3SecretKey = credentials.get("secret");
                log.info("자격증명 갱신 성공");
            } else {
                log.error("자격증명 갱신 실패: null 값 수신");
            }
        } catch (Exception e) {
            log.error("자격증명 갱신 중 예외 발생", e);
        }
    }

    // IAM API를 호출하여 API 인증 토큰(X-Subject-Token)을 발급
    private String fetchApiToken() {
        Map<String, Object> applicationCredential = new HashMap<>();
        applicationCredential.put("id", iamAccessKey);
        applicationCredential.put("secret", iamSecretKey);

        Map<String, Object> identity = new HashMap<>();
        identity.put("methods", Arrays.asList("application_credential"));
        identity.put("application_credential", applicationCredential);

        Map<String, Object> auth = new HashMap<>();
        auth.put("identity", identity);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("auth", auth);

        // POST 요청으로 토큰 발급 및 응답 헤더의 X-Subject-Token 추출
        return webClient.post()
                .uri("https://iam.kakaocloud.com/identity/v3/auth/tokens")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        String token = response.headers().asHttpHeaders().getFirst("X-Subject-Token");
                        return Mono.just(token);
                    } else {
                        return response.createException().flatMap(Mono::error);
                    }
                })
                .block();
    }

    //발급받은 API 토큰을 사용하여 S3 API 사용을 위한 크리덴셜(Access, Secret)을 발급
    private Map<String, String> fetchS3Credentials(String token) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("tenant_id", iamProjectId);

        // S3 크리덴셜 정보 조회
        Map<String, Object> responseMap = webClient.post()
                .uri("https://iam.kakaocloud.com/identity/v3/users/" + iamUserId + "/credentials/OS-EC2")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-Auth-Token", token)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (responseMap == null || !responseMap.containsKey("credential")) {
            log.error("S3 크리덴셜 응답 형식 오류: {}", responseMap);
            return null;
        }

        Object credentialObj = responseMap.get("credential");
        if (!(credentialObj instanceof Map)) {
            log.error("credential 필드의 타입 오류: {}", credentialObj.getClass());
            return null;
        }
        Map<String, Object> credentialMap = (Map<String, Object>) credentialObj;
        Map<String, String> result = new HashMap<>();
        result.put("access", (String) credentialMap.get("access"));
        result.put("secret", (String) credentialMap.get("secret"));
        return result;
    }
}
