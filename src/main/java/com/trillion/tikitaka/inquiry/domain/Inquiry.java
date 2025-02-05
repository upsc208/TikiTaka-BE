package com.trillion.tikitaka.inquiry.domain;

import com.trillion.tikitaka.global.common.BaseEntity;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private boolean status;

    public Inquiry(User requester, InquiryType type, String title, String content) {
        this.requester = requester;
        this.type = type;
        this.title = title;
        this.content = content;
        this.status = false;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
        this.status = true;
    }
}
