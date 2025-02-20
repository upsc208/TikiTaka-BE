package com.trillion.tikitaka.attachment.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticketcomment.domain.TicketComment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE attachments SET deleted_at = NOW() WHERE id = ?")
public class Attachment extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private TicketComment comment;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Builder
    public Attachment(Ticket ticket, TicketComment comment, String fileName, String filePath, Long fileSize) {
        this.ticket = ticket;
        this.comment = comment;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
}
