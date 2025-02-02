package com.trillion.tikitaka.user.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username", "deleted_at"}),
                @UniqueConstraint(columnNames = {"email", "deleted_at"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
public class User extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime lastLoginAt = null;

    private LocalDateTime lastPasswordChangedAt = null;

    private int loginFailCount = 0;

    private boolean locked = false;

    private LocalDateTime lockExpireAt = null;

    @Builder
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(Long userId, String username, String role) {
        this.id = userId;
        this.username = username;
        this.role = Role.valueOf(role);
    }

    public void handleLoginFailure() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.loginFailCount = 0;
            this.locked = true;
            this.lockExpireAt = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.locked = false;
        this.lockExpireAt = null;
    }

    public void updateLastLoginAt(LocalDateTime time) {
        this.lastLoginAt = time;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.lastPasswordChangedAt = LocalDateTime.now();
    }
}
