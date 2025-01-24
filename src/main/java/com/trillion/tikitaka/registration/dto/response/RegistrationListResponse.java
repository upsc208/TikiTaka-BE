package com.trillion.tikitaka.registration.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.trillion.tikitaka.registration.domain.RegistrationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RegistrationListResponse {

    private Long registrationId;
    private String username;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private RegistrationStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @QueryProjection
    public RegistrationListResponse(Long registrationId, String username, String email, RegistrationStatus status, LocalDateTime createdAt) {
        this.registrationId = registrationId;
        this.username = username;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }
}
