package com.trillion.tikitaka.tickettype.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import com.trillion.tikitaka.tickettype.exception.DefaultTicketTypeUnchangeableException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "ticket_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType extends BaseEntity {

    private static final List<String> DEFAULT_TICKET_TYPES = List.of("생성", "변경", "삭제", "기타");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Builder
    public TicketType(String name) {
        this.name = name;
    }

    @PreUpdate
    private void preventDefaultTypeUpdate() {
        if (DEFAULT_TICKET_TYPES.contains(this.name)) {
            throw new DefaultTicketTypeUnchangeableException();
        }
    }
}
