package com.trillion.tikitaka.tickettemplate.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import com.trillion.tikitaka.user.domain.User;
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
    private User manager;    // optional

    // createdAt, updatedAt은 BaseEntity가 자동 관리

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
