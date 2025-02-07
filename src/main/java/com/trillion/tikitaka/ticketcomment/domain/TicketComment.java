package com.trillion.tikitaka.ticketcomment.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticketcomment.exception.InvalidTicketCommentException;
import com.trillion.tikitaka.ticketcomment.exception.UnauthorizedTicketCommentException;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Slf4j
@Builder
@Entity
@Table(name = "ticket_comments")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE ticket_comments SET deleted_at = NOW() WHERE id = ?")
public class TicketComment extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    private String content;

    public void updateComment(String content) {
        this.content = content;
    }

    public void validateTicket(Long ticketId) {
        if (!this.ticket.getId().equals(ticketId)) {
            log.error("[티켓 댓글] 티켓 댓글 ID: {}와 티켓 ID: {} 불일치", this.id, ticketId);
            throw new InvalidTicketCommentException();
        }
    }

    public void validateAuthor(User user) {
        if (!this.author.getId().equals(user.getId())) {
            log.error("[티켓 댓글] 작성자 불일치. 댓글 ID: {}, 작성자 ID: {}", this.id, user.getId());
            throw new UnauthorizedTicketCommentException();
        }
    }
}
