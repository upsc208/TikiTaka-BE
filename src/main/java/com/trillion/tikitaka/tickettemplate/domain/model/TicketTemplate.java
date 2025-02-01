package com.trillion.tikitaka.tickettemplate.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 티켓 템플릿 엔티티
 * managerId 제외 전부 필수
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_templates")
public class TicketTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필수
    @Column(name = "template_title", nullable = false, length = 100)
    private String templateTitle;

    // 필수
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 필수
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // 필수 (유효한 type_id)
    @Column(name = "type_id", nullable = false)
    private Long typeId;

    // 필수 (유효한 1차카테고리)
    @Column(name = "first_category_id", nullable = false)
    private Long firstCategoryId;

    // 필수 (유효한 2차카테고리)
    @Column(name = "second_category_id", nullable = false)
    private Long secondCategoryId;

    // 필수 (유효한 user)
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    // 선택 (nullable)
    @Column(name = "manager_id")
    private Long managerId;

    // 필수
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public TicketTemplate(String templateTitle,
                          String title,
                          String description,
                          Long typeId,
                          Long firstCategoryId,
                          Long secondCategoryId,
                          Long requesterId,
                          Long managerId,
                          LocalDateTime createdAt) {
        this.templateTitle = templateTitle;
        this.title = title;
        this.description = description;
        this.typeId = typeId;
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
        this.requesterId = requesterId;
        this.managerId = managerId;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }
}
