package com.trillion.tikitaka.statistics.domain;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.global.common.BaseEntity;
import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_statistics")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE monthly_statistics SET deleted_at = NOW() WHERE id = ?")
public class MonthlyStatistics extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_year", nullable = false)
    private int statYear;

    @Column(name = "stat_month", nullable = false)
    private int statMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private TicketType type;

    @Column(name = "total_created", nullable = false)
    private int totalCreated = 0;

    @Column(name = "urgent_tickets", nullable = false)
    private int urgentTickets = 0;

    @Column(name = "total_completed", nullable = false)
    private int totalCompleted = 0;

    @Column(name = "in_progress_count", nullable = false)
    private int inProgressCount = 0;

    @Column(name = "average_completion_time", nullable = false)
    private float averageCompletionTime = 0f;

    @Column(name = "completion_ratio", nullable = false)
    private float completionRatio = 0f;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public void updateStatistics(int totalCreated, int totalCompleted, int urgentTickets, int inProgressCount, float completionRatio) {
        this.totalCreated = totalCreated;
        this.totalCompleted = totalCompleted;
        this.urgentTickets = urgentTickets;
        this.inProgressCount = inProgressCount;
        this.completionRatio = completionRatio;
    }

}