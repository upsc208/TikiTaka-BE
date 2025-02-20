package com.trillion.tikitaka.notification.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String messageJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String errorMessage;

    @Builder
    public Notification(User receiver, String messageJson, NotificationType type, NotificationStatus status) {
        this.receiver = receiver;
        this.messageJson = messageJson;
        this.type = type;
        this.status = status;
        this.errorMessage = "";
    }

    public void updateStatus(NotificationStatus status) {
        this.status = status;
    }

    public void updateMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
