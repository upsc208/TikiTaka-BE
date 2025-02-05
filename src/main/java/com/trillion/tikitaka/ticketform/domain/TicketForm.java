package com.trillion.tikitaka.ticketform.domain;

import com.trillion.tikitaka.category.domain.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_forms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketForm {

    @EmbeddedId
    private TicketFormId id;

    @MapsId("firstCategoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_category_id", nullable = false)
    private Category firstCategory;

    @MapsId("secondCategoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_category_id", nullable = false)
    private Category secondCategory;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false, length = 1000)
    private String mustDescription;

    @Builder
    public TicketForm(Category firstCategory, Category secondCategory, String description, String mustDescription) {
        this.id = new TicketFormId(firstCategory.getId(), secondCategory.getId());
        this.firstCategory = firstCategory;
        this.secondCategory = secondCategory;
        this.description = description;
        this.mustDescription = mustDescription;
    }

    public void update(String newDescription, String newMustDescription) {
        this.description = newDescription;
        this.mustDescription = newMustDescription;
    }
}
