package com.trillion.tikitaka.infrastructure.kakaowork;

import com.trillion.tikitaka.notification.dto.request.KakaoWorkConversationRequest;
import com.trillion.tikitaka.notification.dto.request.KakaoWorkMessageRequest;
import com.trillion.tikitaka.notification.dto.request.KakaoWorkUserRequest;
import com.trillion.tikitaka.notification.exception.KakaoWorkFetchingUserIdException;
import com.trillion.tikitaka.notification.exception.KakaoWorkOpeningConversationException;
import com.trillion.tikitaka.notification.exception.KakaoWorkSendingMessageException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class KakaoWorkClient {

    private final WebClient kakaoWorkWebClient;

    public KakaoWorkClient(@Qualifier("kakaoWorkWebClient") WebClient kakaoWorkWebClient) {
        this.kakaoWorkWebClient = kakaoWorkWebClient;
    }

    public Mono<KakaoWorkUserRequest> findUserIdByEmail(String email) {
        return kakaoWorkWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/users.find_by_email")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new KakaoWorkFetchingUserIdException())))
                .bodyToMono(KakaoWorkUserRequest.class);
    }

    public Mono<KakaoWorkConversationRequest> openConversation(String userId) {
        return kakaoWorkWebClient.post()
                .uri("/v1/conversations.open")
                .bodyValue("{\"user_id\": \"" + userId + "\"}")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new KakaoWorkOpeningConversationException())))
                .bodyToMono(KakaoWorkConversationRequest.class);
    }

    public Mono<Void> sendMessage(KakaoWorkMessageRequest messageRequest) {
        return kakaoWorkWebClient.post()
                .uri("/v1/messages.send")
                .bodyValue(messageRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new KakaoWorkSendingMessageException())))
                .bodyToMono(Void.class);
    }
}
