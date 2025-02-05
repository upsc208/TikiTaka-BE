package com.trillion.tikitaka.inquiry.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @Builder
    public Inquiry(String content, User writer) {
        this.content = content;
        this.writer = writer;
    }
}
