package com.trillion.tikitaka.ticketcomment.domain;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticketcomment.exception.InvalidTicketCommentException;
import com.trillion.tikitaka.ticketcomment.exception.UnauthorizedTicketCommentException;
import com.trillion.tikitaka.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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
            throw new InvalidTicketCommentException();
        }
    }

    public void validateAuthor(User user) {
        if (!this.author.getId().equals(user.getId())) {
            throw new UnauthorizedTicketCommentException();
        }
    }
}
