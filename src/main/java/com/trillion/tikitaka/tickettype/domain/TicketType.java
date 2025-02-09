package com.trillion.tikitaka.tickettype.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.transaction.TransactionSystemException;

@Entity
@Table(name = "ticket_types",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted_at"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE ticket_types SET deleted_at = NOW() WHERE id = ?")
public class TicketType extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean defaultType = false;

    @Builder
    public TicketType(String name) {
        this.name = name;
    }

    @PreUpdate
    private void preventDefaultTypeUpdate() {
        if (this.defaultType) {
            throw new TransactionSystemException("기본 티켓 유형은 변경할 수 없습니다.");
        }
    }

    public void updateName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public TicketType(Long id, String name) {
        this.id = id;  // ✅ ID를 직접 설정 가능하도록 수정
        this.name = name;
    }
}
