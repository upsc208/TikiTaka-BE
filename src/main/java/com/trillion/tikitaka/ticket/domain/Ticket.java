package com.trillion.tikitaka.ticket.domain;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "tickets")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE ticket SET deleted_at = NOW() WHERE id = ?")
public class Ticket extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_category_id")
    private Category firstCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_category_id")
    private Category secondCategory;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false)
    @Builder.Default
    private Boolean urgent = false;

    public void update(EditTicketRequest request, TicketType ticketType, Category firstCategory, Category secondCategory) {
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getDescription() != null) this.description = request.getDescription();
        if (firstCategory != null) this.firstCategory = firstCategory;
        if (secondCategory != null) this.secondCategory = secondCategory;
        if (request.getUrgent() != null) this.urgent = request.getUrgent();
        if (ticketType != null) this.ticketType = ticketType;
    }


    public void updateSetting(EditSettingRequest request){
        if (request.getPriority() != null) this.priority = request.getPriority();
        if (request.getManagerId() != null) this.manager = manager;
        if (request.getDeadline() != null) this.deadline = request.getDeadline();
    }

    public void updateStatus(Status status){
        this.status = status;
    }

    public enum Priority {
        HIGH, MIDDLE, LOW
    }

    public enum Status {
        PENDING, IN_PROGRESS, REVIEW, DONE, REJECTED
    }
}



