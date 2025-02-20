package com.trillion.tikitaka.notification.application;

import com.trillion.tikitaka.infrastructure.kakaowork.KakaoWorkClient;
import com.trillion.tikitaka.notification.domain.Notification;
import com.trillion.tikitaka.notification.domain.NotificationStatus;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.notification.dto.request.KakaoWorkMessageRequest;
import com.trillion.tikitaka.notification.dto.response.Block;
import com.trillion.tikitaka.notification.infrastructure.NotificationRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoWorkNotificationService {

    private final KakaoWorkClient kakaoWorkClient;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final BlockJsonConverter blockJsonConverter;

    public Mono<Void> sendKakaoWorkNotification(String email, String text, List<Block> blocks, NotificationType type) {
        log.info("[알림 전송 시작] 이메일: {}, 알림 유형: {}, 메시지: {}", email, type, text);
        String blockJson = blockJsonConverter.convertBlocksToJson(blocks);

        ConcurrentHashMap<String, String> errorMap = new ConcurrentHashMap<>();

        return Mono.fromCallable(() -> {
                    User receiver = userRepository.findByEmail(email).orElse(null);
                    if (receiver == null) {
                        log.warn("[수신자 조회 실패] 이메일 {}에 해당하는 사용자를 찾을 수 없음", email);
                    } else {
                        log.info("[수신자 조회 성공] 사용자 ID: {}", receiver.getId());
                    }

                    Notification notification = new Notification(receiver, blockJson, type, NotificationStatus.PENDING);
                    return notificationRepository.save(notification);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(notification ->
                        // ─────────  사용자 조회  ─────────
                        kakaoWorkClient.findUserIdByEmail(email)
                                .doOnSuccess(userResponse ->
                                        log.info("[사용자 조회 성공] userId={}", userResponse.getUser().getId())
                                )
                                .doOnError(e -> {
                                    String errMsg = "사용자 조회 오류: " + e.getMessage();
                                    log.error("[사용자 조회 오류] {}", errMsg, e);
                                    errorMap.putIfAbsent("에러", errMsg);
                                })
                                .onErrorResume(Mono::error)

                                // ───────── 채팅방 생성 ─────────
                                .flatMap(userResponse ->
                                        kakaoWorkClient.openConversation(userResponse.getUser().getId())
                                                .doOnSuccess(conv -> log.info("[채팅방 생성 성공] convId={}", conv.getConversation().getId()))
                                                .doOnError(e -> {
                                                    String errMsg = "채팅방 생성 오류: " + e.getMessage();
                                                    log.error("[채팅방 생성 오류] {}", errMsg, e);
                                                    errorMap.putIfAbsent("에러", errMsg);
                                                })
                                                .onErrorResume(Mono::error)
                                )

                                // ───────── 메시지 전송 ─────────
                                .flatMap(convResponse -> {
                                    KakaoWorkMessageRequest request = new KakaoWorkMessageRequest(
                                            convResponse.getConversation().getId(), text, blocks
                                    );
                                    return kakaoWorkClient.sendMessage(request)
                                            .doOnSuccess(unused -> log.info("[메시지 전송 성공] email={}", email))
                                            .doOnError(e -> {
                                                String errMsg = "메시지 전송 오류: " + e.getMessage();
                                                log.error("[메시지 전송 오류] {}", errMsg, e);
                                                errorMap.putIfAbsent("에러", errMsg);
                                            })
                                            .onErrorResume(Mono::error);
                                })

                                // ───────── 전체 재시도 ─────────
                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                                        .maxBackoff(Duration.ofSeconds(60))
                                        .jitter(0.5)
                                        .doBeforeRetry(signal ->
                                                log.warn("[재시도] {}번째 재시도 시작: {}",
                                                        signal.totalRetries() + 1, signal.failure().getMessage())
                                        )
                                )

                                // ───────── 모든 단계 성공 ─────────
                                .doOnSuccess(unused -> {
                                    log.info("[알림 전송 최종 성공] email={}", email);
                                    Mono.fromCallable(() -> {
                                                notification.updateStatus(NotificationStatus.SUCCESS);
                                                notification.updateMessage("");
                                                return notificationRepository.save(notification);
                                            })
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .subscribe();
                                })

                                // ───────── 최종 실패 ─────────
                                .doOnError(e -> {
                                    log.error("[알림 전송 최종 실패] email={}, 에러={}", email, e.getMessage(), e);
                                    Mono.fromCallable(() -> {
                                                notification.updateStatus(NotificationStatus.FAIL);
                                                notification.updateMessage(errorMap.getOrDefault("에러", ""));
                                                return notificationRepository.save(notification);
                                            })
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .subscribe();
                                })
                                .onErrorResume(e -> Mono.empty())
                                .then()
                )
                .then();
    }
}
