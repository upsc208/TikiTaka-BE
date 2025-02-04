package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.infrastructure.kakaowork.KakaoWorkClient;
import com.trillion.tikitaka.notification.dto.request.KakaoWorkMessageRequest;
import com.trillion.tikitaka.notification.dto.response.Block;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KakaoWorkNotificationService {

    private final KakaoWorkClient kakaoWorkClient;

    public Mono<Void> sendKakaoWorkNotification(String email, List<Block> blocks) {
        return kakaoWorkClient.findUserIdByEmail(email)
                .flatMap(userResponse -> kakaoWorkClient.openConversation(userResponse.getUser().getId()))
                .flatMap(conversationResponse -> kakaoWorkClient.sendMessage(
                        new KakaoWorkMessageRequest(conversationResponse.getConversation().getId(), "알림이 전송되었습니다.", blocks)
                ))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(10))
                        .maxBackoff(Duration.ofSeconds(60))
                        .jitter(0.5)
                )
                .onErrorResume(e -> {
                    // TODO: 전송 실패 시 처리
                    return Mono.empty();
                });
    }
}
