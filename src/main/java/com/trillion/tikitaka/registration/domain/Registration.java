package com.trillion.tikitaka.registration.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "registrations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(nullable = true)
    private String reason;

    @Builder
    public Registration(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public void approve(String approveReason) {
        this.status = RegistrationStatus.APPROVED;
        this.reason = approveReason;
    }

    public void reject(String rejectReason) {
        this.status = RegistrationStatus.REJECTED;
        this.reason = rejectReason;
    }
}
