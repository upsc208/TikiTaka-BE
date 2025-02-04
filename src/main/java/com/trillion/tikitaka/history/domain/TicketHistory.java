package com.trillion.tikitaka.history.domain;

import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User updatedBy;

    @CreatedDate
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 50)
    private UpdateType updateType;



    // 변경 유형 ENUM 정의
    public enum UpdateType {
        TICKET_CREATED, TICKET_APPROVED, TICKET_EDITED, TYPE_CHANGE, STATUS_CHANGE, MANAGER_CHANGE, PRIORITY_CHANGE, CATEGORY_CHANGE, DEADLINE_CHANGE, TICKET_DELETE ,OTHER
    }

    // 변경 이력 생성 메서드
    public static TicketHistory createHistory(Ticket ticket, User updatedBy, UpdateType updateType) {
        return TicketHistory.builder()
                .ticket(ticket)
                .updatedBy(updatedBy)
                .updateType(updateType)
                .build();
    }
}
