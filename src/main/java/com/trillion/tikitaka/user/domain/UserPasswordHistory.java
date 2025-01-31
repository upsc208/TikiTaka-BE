package com.trillion.tikitaka.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_password_history")
public class UserPasswordHistory {

    @Id
    @Column(name = "user_id") // 기본 키를 user_id로 설정
    private Long userId;

    @OneToOne
    @MapsId // user_id를 기본 키로 사용
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String oldPassword;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public UserPasswordHistory(User user, String oldPassword) {
        this.user = user;
        this.userId = user.getId();
        this.oldPassword = oldPassword;
        this.changedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.oldPassword = newPassword;
        this.changedAt = LocalDateTime.now();
    }
}
