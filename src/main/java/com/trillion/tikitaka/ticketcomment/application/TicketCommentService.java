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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;

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
        ticketCommentRepository.save(comment);
        ticketCommentRepository.flush();

        if (files != null && !files.isEmpty()) {
            log.info("[티켓 댓글 생성] 첨부 파일 업로드 시작");
            fileService.uploadFilesForComment(files, comment);
        }

        // 댓글 작성 메시지 작성 시 비동기 처리를 위한 연관 객체 강제 초기화 (프록시 초기화)
        if (ticket.getFirstCategory() != null) ticket.getFirstCategory().getName();
        if (ticket.getSecondCategory() != null) ticket.getSecondCategory().getName();
        if (ticket.getTicketType() != null) ticket.getTicketType().getName();
        ticket.getManager().getUsername();
        ticket.getRequester().getUsername();

        if (author.getRole() == Role.USER) {
            eventPublisher.publishEvent(new CommentCreateEvent(this, ticket.getManager().getEmail(), ticket, author.getUsername()));
        } else {
            eventPublisher.publishEvent(new CommentCreateEvent(this, ticket.getRequester().getEmail(), ticket, author.getUsername()));
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
