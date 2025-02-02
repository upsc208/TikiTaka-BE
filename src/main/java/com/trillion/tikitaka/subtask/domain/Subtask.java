package com.trillion.tikitaka.subtask.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.domain.Ticket;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(name = "subtasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE subtasks SET deleted_at = NOW() WHERE id = ?")
public class Subtask extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_ticket_id", nullable = false)
    private Ticket parentTicket;

    @Column
    @Builder.Default
    private Boolean is_Done = false;


    public boolean isDone() {
        return is_Done;
    }
    public void updateIsDone(Boolean check){this.is_Done = check;}
    public void updateDescription(String description){
        this.description = description;
    }

}
