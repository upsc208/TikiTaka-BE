package com.trillion.tikitaka.ticket.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.dto.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
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
    @Builder.Default
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
    @Builder.Default
    private Boolean urgent = false;


    public void update(EditTicketRequest request, TicketType ticketType) {
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getDescription() != null) this.description = request.getDescription();
        if (request.getFirstCategoryId() != null) this.firstCategoryId = request.getFirstCategoryId();
        if (request.getSecondCategoryId() != null) this.secondCategoryId = request.getSecondCategoryId();
        if (request.getUrgent() != null) this.urgent = request.getUrgent();
        if (ticketType != null) this.ticketType = ticketType;
    }
    public void updateSetting(EditSettingRequest request){
        if (request.getPriority() != null) this.priority = request.getPriority();
        if (request.getManagerId() != null) this.managerId = request.getManagerId();
        if (request.getDeadline() != null) this.deadline = request.getDeadline();
    }

    public void updateStatus(Status status){
        this.status = status;
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
