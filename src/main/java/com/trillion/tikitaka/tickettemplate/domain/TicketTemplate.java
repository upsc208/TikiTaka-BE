package com.trillion.tikitaka.tickettemplate.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_templates")
public class TicketTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String templateTitle;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private TicketType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_category_id", nullable = false)
    private Category firstCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_category_id", nullable = false)
    private Category secondCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Builder
    public TicketTemplate(String templateTitle,
                          String title,
                          String description,
                          TicketType type,
                          Category firstCategory,
                          Category secondCategory,
                          User requester,
                          User manager
    ) {
        this.templateTitle = templateTitle;
        this.title = title;
        this.description = description;
        this.type = type;
        this.firstCategory = firstCategory;
        this.secondCategory = secondCategory;
        this.requester = requester;
        this.manager = manager;
    }

    public void update(String templateTitle,
                       String title,
                       String description,
                       TicketType type,
                       Category firstCategory,
                       Category secondCategory,
                       User requester,
                       User manager
    ) {
        this.templateTitle = templateTitle;
        this.title = title;
        this.description = description;
        this.type = type;
        this.firstCategory = firstCategory;
        this.secondCategory = secondCategory;
        this.requester = requester;
        this.manager = manager;
    }
}
