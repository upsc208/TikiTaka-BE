package com.trillion.tikitaka.notification.infrastructure;

import com.trillion.tikitaka.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
