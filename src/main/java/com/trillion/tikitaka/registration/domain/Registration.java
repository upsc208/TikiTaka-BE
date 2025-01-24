package com.trillion.tikitaka.registration.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "registrations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username"}),
                @UniqueConstraint(columnNames = {"email"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Builder
    public Registration(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public void approve() {
        this.status = RegistrationStatus.APPROVED;
    }

    public void reject() {
        this.status = RegistrationStatus.REJECTED;
    }
}
