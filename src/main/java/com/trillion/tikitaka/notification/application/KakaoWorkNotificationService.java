package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.infrastructure.kakaowork.KakaoWorkClient;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.notification.dto.request.KakaoWorkMessageRequest;
import com.trillion.tikitaka.notification.dto.response.Block;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KakaoWorkNotificationService {

    private final KakaoWorkClient kakaoWorkClient;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 10000,
                    multiplier = 2
            )
    )
    public Mono<Void> sendKakaoWorkNotification(String email, List<Block> blocks) {
        return kakaoWorkClient.findUserIdByEmail(email)
                .flatMap(userResponse -> kakaoWorkClient.openConversation(userResponse.getUser().getId()))
                .flatMap(conversationResponse -> kakaoWorkClient.sendMessage(
                        new KakaoWorkMessageRequest(conversationResponse.getConversation().getId(), "알림이 전송되었습니다.", blocks)
                ));
    }

    @Recover
    public void recover(Exception e, String email, NotificationType type) {
        // TODO: 알림 DB에 실패 이력 저장
    }
}
