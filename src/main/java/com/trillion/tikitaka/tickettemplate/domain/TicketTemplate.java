package com.trillion.tikitaka.tickettemplate.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.trillion.tikitaka.user.domain.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_templates")
@EntityListeners(AuditingEntityListener.class)
public class TicketTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String templateTitle;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long typeId;

    @Column(nullable = false)
    private Long firstCategoryId;

    @Column(nullable = false)
    private Long secondCategoryId;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;  // 필수

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public TicketTemplate(String templateTitle,
                          String title,
                          String description,
                          Long typeId,
                          Long firstCategoryId,
                          Long secondCategoryId,
                          User requester,
                          User manager
    ) {
        this.templateTitle = templateTitle;
        this.title = title;
        this.description = description;
        this.typeId = typeId;
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
        this.requester = requester;
        this.manager = manager;
    }

    public void update(String templateTitle,
                       String title,
                       String description,
                       Long typeId,
                       Long firstCategoryId,
                       Long secondCategoryId,
                       User requester,
                       User manager
    ) {
        this.templateTitle = templateTitle;
        this.title = title;
        this.description = description;
        this.typeId = typeId;
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
        this.requester = requester;
        this.manager = manager;
    }
}
