package com.trillion.tikitaka.ticketcomment.application;

import com.trillion.tikitaka.attachment.application.FileService;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.notification.event.CommentCreateEvent;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticketcomment.domain.TicketComment;
import com.trillion.tikitaka.ticketcomment.dto.request.TicketCommentRequest;
import com.trillion.tikitaka.ticketcomment.dto.response.TicketCommentResponse;
import com.trillion.tikitaka.ticketcomment.exception.TicketCommentNotFoundException;
import com.trillion.tikitaka.ticketcomment.exception.UnauthorizedTicketCommentException;
import com.trillion.tikitaka.ticketcomment.infrastructure.TicketCommentRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.trillion.tikitaka.notification.dto.response.ButtonBlock.END_POINT;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;
    private final UserRepository userRepository;

    @Transactional
    public void createTicketComment(Long ticketId, TicketCommentRequest request, List<MultipartFile> files, CustomUserDetails userDetails) {
        log.info("[티켓 댓글 생성] 티켓 ID: {}, 작성자 ID: {}", ticketId, userDetails.getUser().getId());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        User author = userDetails.getUser();
        if (!ticket.canComment(author)) {
            log.error("[티켓 댓글 생성] 권한 없음");
            throw new UnauthorizedTicketCommentException();
        }

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(author)
                .content(request.getContent())
                .build();
        ticketCommentRepository.saveAndFlush(comment);

        if (files != null && !files.isEmpty()) {
            log.info("[티켓 댓글 생성] 첨부 파일 업로드 시작");
            fileService.uploadFilesForComment(files, comment);
        }

        if (author.getRole() == Role.USER) {
            User manager = (ticket.getManager() != null) ? userRepository.findById(ticket.getManager().getId()).orElse(null) : null;

            if (manager != null) {
                String url = END_POINT + "/manager/detail/" + ticket.getId();

                eventPublisher.publishEvent(
                        new CommentCreateEvent(
                                this,
                                manager.getEmail(),
                                ticket.getId(),
                                ticket.getTitle(),
                                ticket.getFirstCategory() == null ? null : ticket.getFirstCategory().getName(),
                                ticket.getSecondCategory() == null ? null : ticket.getSecondCategory().getName(),
                                ticket.getTicketType() == null ? null : ticket.getTicketType().getName(),
                                author.getUsername(),
                                url
                        )
                );
            }
        } else {
            User requester = ticket.getRequester();
            String url = END_POINT + "/user/detail/" + ticket.getId();

            eventPublisher.publishEvent(
                    new CommentCreateEvent(
                            this,
                            requester.getEmail(),
                            ticket.getId(),
                            ticket.getTitle(),
                            ticket.getFirstCategory() == null ? null : ticket.getFirstCategory().getName(),
                            ticket.getSecondCategory() == null ? null : ticket.getSecondCategory().getName(),
                            ticket.getTicketType() == null ? null : ticket.getTicketType().getName(),
                            author.getUsername(),
                            url
                    )
            );
        }
    }

    public List<TicketCommentResponse> getTicketComments(Long ticketId, CustomUserDetails userDetails) {
        log.info("[티켓 댓글 조회] 티켓 ID: {}, 요청자 ID: {}", ticketId, userDetails.getUser().getId());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        if (!ticket.canComment(userDetails.getUser())) {
            log.error("[티켓 댓글 조회] 권한 없음");
            throw new UnauthorizedTicketCommentException();
        }

        return ticketCommentRepository.getTicketComments(ticketId);
    }

    @Transactional
    public void updateTicketComment(Long ticketId, Long commentId, TicketCommentRequest request, CustomUserDetails userDetails) {
        log.info("[티켓 댓글 수정] 티켓 ID: {}, 댓글 ID: {}, 작성자 ID: {}", ticketId, commentId, userDetails.getUser().getId());
        TicketComment comment = ticketCommentRepository.findById(commentId)
                .orElseThrow(TicketCommentNotFoundException::new);

        comment.validateTicket(ticketId);
        comment.validateAuthor(userDetails.getUser());

        comment.updateComment(request.getContent());
    }

    @Transactional
    public void deleteTicketComment(Long ticketId, Long commentId, CustomUserDetails userDetails) {
        log.info("[티켓 댓글 삭제] 티켓 ID: {}, 댓글 ID: {}, 작성자 ID: {}", ticketId, commentId, userDetails.getUser().getId());
        TicketComment comment = ticketCommentRepository.findById(commentId)
                .orElseThrow(TicketCommentNotFoundException::new);

        comment.validateTicket(ticketId);
        comment.validateAuthor(userDetails.getUser());

        ticketCommentRepository.delete(comment);
    }
}
