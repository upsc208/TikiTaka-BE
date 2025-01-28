package com.trillion.tikitaka.ticket.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"title", "deleted_at"})
        })
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE ticket SET deleted_at = NOW() WHERE id = ?")
public class Ticket extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY) // 연관 관계 설정
    @JoinColumn(name = "type_id", nullable = false) // 매핑할 외래 키
    private TicketType ticketType;

    private Long firstCategoryId;

    private Long secondCategoryId;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private Long requesterId;

    private Long managerId;

    @Column(nullable = false)
    private Boolean urgent = false;


    public void updateFrom(Ticket updatedTicket) {
        this.title = updatedTicket.title;
        this.description = updatedTicket.description;
        this.priority = updatedTicket.priority;
        this.status = updatedTicket.status;
        this.ticketType = updatedTicket.ticketType;
        this.firstCategoryId = updatedTicket.firstCategoryId;
        this.secondCategoryId = updatedTicket.secondCategoryId;
        this.deadline = updatedTicket.deadline;
        this.requesterId = updatedTicket.requesterId;
        this.managerId = updatedTicket.managerId;
        this.urgent = updatedTicket.urgent;
    }


    // 우선순위 ENUM 정의
    public enum Priority {
        HIGH, MIDDLE, LOW
    }

    // 상태 ENUM 정의
    public enum Status {
        PENDING, APPROVED, IN_PROGRESS, REVIEW, DONE, REJECTED
    }
}
