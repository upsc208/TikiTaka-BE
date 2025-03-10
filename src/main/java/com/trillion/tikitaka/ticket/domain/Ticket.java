package com.trillion.tikitaka.ticket.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@Entity
@Table(name = "tickets")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE tickets SET deleted_at = NOW() WHERE id = ?")
public class Ticket extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(nullable = false, length = 5000, columnDefinition = "LONGTEXT")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
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

    @Column
    @Builder.Default
    private Double progress = null;

    public void update(EditTicketRequest request,TicketType ticketType, Category firstCategory, Category secondCategory) {
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getDescription() != null) this.description = request.getDescription();
        if (request.getDeadline() != null) this.deadline = request.getDeadline();
        if (firstCategory != null) this.firstCategory = firstCategory;
        if (secondCategory != null) this.secondCategory = secondCategory;
        if (request.getUrgent() != null) this.urgent = request.getUrgent();
        if(ticketType != null) this.ticketType = ticketType;
    }
    public void updateNullCategory(EditTicketRequest request,TicketType ticketType, Category firstCategory, Category secondCategory) {
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getDescription() != null) this.description = request.getDescription();
        if (request.getDeadline() != null) this.deadline = request.getDeadline();
        this.firstCategory = firstCategory;
        this.secondCategory = secondCategory;
        if (request.getUrgent() != null) this.urgent = request.getUrgent();
        if(ticketType != null) this.ticketType = ticketType;
    }

    //사용자
    public void updateCategory(Category firstCategory, Category secondCategory){
        this.firstCategory = firstCategory;
        this.secondCategory = secondCategory;
    }

    public void updateUrgent(EditTicketRequest request) {
        if (request.getUrgent() != null) this.urgent = request.getUrgent();
    }

    public void updateType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    //담당자
    public void updatePriority(Priority priority){
        if (priority != null) this.priority = priority;
    }

    public void updateManager(User manager){
        if (manager != null) this.manager = manager;
    }

    public void updateDaedlineForManager(LocalDateTime dateline){
        if (dateline != null) this.deadline = dateline;
    }

    public void updateStatus(Status status){
        this.status = status;
    }

    public boolean canComment(User user) {
        if (user.getRole() == Role.USER) {
            return Objects.equals(this.requester.getId(), user.getId());
        }
        return true;
    }

    public void updateProgress(Double progress) {
        this.progress = progress;
    }

    public enum Priority {
        HIGH, MIDDLE, LOW
    }

    public enum Status {
        PENDING, IN_PROGRESS, REVIEW, DONE, REJECTED
    }
}
