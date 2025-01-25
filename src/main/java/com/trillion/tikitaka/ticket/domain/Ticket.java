package com.trillion.tikitaka.ticket.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket")
public class Ticket extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private Long typeId;

    private Long firstCategoryId;

    private Long secondCategoryId;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private Long requesterId;

    private Long managerId;

    @Column(nullable = false)
    private Boolean urgent = false;

    @Column(nullable = false)
    private Boolean is_active = true;

    public void updateFrom(Ticket updatedTicket) {
        this.title = updatedTicket.title;
        this.description = updatedTicket.description;
        this.priority = updatedTicket.priority;
        this.status = updatedTicket.status;
        this.typeId = updatedTicket.typeId;
        this.firstCategoryId = updatedTicket.firstCategoryId;
        this.secondCategoryId = updatedTicket.secondCategoryId;
        this.deadline = updatedTicket.deadline;
        this.requesterId = updatedTicket.requesterId;
        this.managerId = updatedTicket.managerId;
        this.urgent = updatedTicket.urgent;
    }
    public void activate() {
        this.is_active = true;
    }

    public void deactivate() {
        this.is_active = false;
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
