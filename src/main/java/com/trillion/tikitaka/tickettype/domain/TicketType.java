package com.trillion.tikitaka.tickettype.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.TransactionSystemException;

@Entity
@Table(name = "ticket_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

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
}
