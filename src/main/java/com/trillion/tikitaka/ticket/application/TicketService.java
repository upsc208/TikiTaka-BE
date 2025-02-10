package com.trillion.tikitaka.ticket.application;


import com.trillion.tikitaka.attachment.application.FileService;
import com.trillion.tikitaka.attachment.dto.response.AttachmentResponse;
import com.trillion.tikitaka.attachment.infrastructure.AttachmentRepository;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.history.application.HistoryService;
import com.trillion.tikitaka.history.domain.TicketHistory;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.notification.event.TicketCreationEvent;
import com.trillion.tikitaka.notification.event.TicketUpdateEvent;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.request.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.request.EditCategory;
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
import com.trillion.tikitaka.ticket.dto.response.PendingTicketResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketResponse;
import com.trillion.tikitaka.ticket.exception.InvalidTicketManagerException;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.exception.UnauthorizedTicketAccessException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CategoryRepository categoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final HistoryService historyService;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createTicket(CreateTicketRequest request, List<MultipartFile> files, CustomUserDetails userDetails) {
        log.info("[티켓 생성 요청] 요청자: {}, 티켓 유형: {}, 1차/2차 카테고리: {}/{}",
                userDetails.getUsername(), request.getTypeId(), request.getFirstCategoryId(), request.getSecondCategoryId());
        TicketType ticketType = getTicketTypeOrThrow(request.getTypeId());
        Category firstCategory = getCategoryOrNull(request.getFirstCategoryId());
        Category secondCategory = getCategoryOrNull(request.getSecondCategoryId());
        validateCategoryRelation(firstCategory, secondCategory);

        User requester = getUserOrThrow(userDetails.getId());
        User manager = request.getManagerId() != null ? getUserOrThrowForManager(request.getManagerId()) : null;

        Ticket ticket = buildTicket(request, requester, manager, ticketType, firstCategory, secondCategory);
        ticketRepository.save(ticket);
        ticketRepository.flush();

        if (files != null && !files.isEmpty()) {
            log.info("[티켓 생성] 첨부 파일 업로드 시작");
            fileService.uploadFilesForTicket(files, ticket);
        }

        if (ticket.getManager() != null){
            eventPublisher.publishEvent(
                    new TicketCreationEvent(this, ticket.getManager().getEmail(), ticket, NotificationType.TICKET_CREATE)
            );
        } else {
            List<User> managers = userRepository.findAllByRole(Role.MANAGER);
            for (User m : managers) {
                eventPublisher.publishEvent(
                        new TicketCreationEvent(this, m.getEmail(), ticket, NotificationType.TICKET_CREATE)
                );
            }
        }

        return ticket.getId();
    }

    public TicketCountByStatusResponse countTicketsByStatus(CustomUserDetails userDetails) {
        log.info("[상태별 티켓 수 조회] 요청자: {}", userDetails.getUsername());
        String role = userDetails.getUser().getRole().toString();
        Long requesterId = "USER".equals(role) ? userDetails.getUser().getId() : null;

        return ticketRepository.countTicketsByStatus(requesterId, role);
    }

    public Page<TicketListResponse> getTicketList(Pageable pageable, Ticket.Status status, Long firstCategoryId,
                                                  Long secondCategoryId, Long ticketTypeId, Long managerId, Long requesterId,
                                                  String dateOption, String sort, CustomUserDetails userDetails) {
        log.info("[티켓 목록 조회] 요청자: {}, 상태: {}, 1차/2차 카테고리: {}/{}, 티켓 유형: {}, 담당자: {}, 요청자: {}, 정렬: {}, 날짜 옵션: {}",
                userDetails.getUsername(), status, firstCategoryId, secondCategoryId, ticketTypeId, managerId, requesterId, sort, dateOption);
        String role = userDetails.getUser().getRole().toString();

        if ("USER".equals(role)) {
            requesterId = userDetails.getUser().getId();
            if (managerId != null) {
                log.error("[티켓 목록 조회] 사용자 권한으로 담당자 조회 불가");
                throw new UnauthorizedTicketAccessException();
            }
        }

        validateTicketType(ticketTypeId);
        validateCategoryRelation(firstCategoryId, secondCategoryId);
        validateUserExistence(requesterId);
        validateUserExistence(managerId);

        return ticketRepository.getTicketList(
                pageable, status, firstCategoryId, secondCategoryId, ticketTypeId, managerId, requesterId, role, sort, dateOption
        );
    }

    public TicketResponse getTicket(Long ticketId, CustomUserDetails userDetails) {
        log.info("[티켓 조회] 요청자: {}, 티켓 ID: {}", userDetails.getUsername(), ticketId);
        String role = userDetails.getUser().getRole().toString();
        Long userId = userDetails.getUser().getId();

        TicketResponse response = ticketRepository.getTicket(ticketId, userId, role);
        if (response == null) {
            log.error("[티켓 조회] 티켓 ID: {} 조회 실패", ticketId);
            throw new TicketNotFoundException();
        }

        List<AttachmentResponse> attachmentResponse = attachmentRepository.getTicketAttachments(ticketId);
        response.setAttachments(attachmentResponse);

        if ("USER".equals(role)) {
            log.info("[티켓 조회] 사용자 권한으로 티켓 조회 - 우선순위 정보 제거");
            response.setPriority(null);
        }

        return response;
    }

    @Transactional
    public void editTicket(EditTicketRequest request, Long ticketId, CustomUserDetails userDetails) {
        log.info("[사용자 티켓 수정] 요청자: {}, 티켓 ID: {}", userDetails.getUsername(), ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        TicketType ticketType = request.getTicketTypeId() != null
                ? ticketTypeRepository.findById(request.getTicketTypeId()).orElseThrow(TicketTypeNotFoundException::new)
                : ticket.getTicketType();

        Category firstCategory = request.getFirstCategoryId() != null
                ? categoryRepository.findById(request.getFirstCategoryId()).orElseThrow(CategoryNotFoundException::new)
                : null;

        Category secondCategory = request.getSecondCategoryId() != null
                ? categoryRepository.findById(request.getSecondCategoryId()).orElseThrow(CategoryNotFoundException::new)
                : null;

        User user = userDetails.getUser();

        ticket.update(request, ticketType, firstCategory, secondCategory);
        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.TICKET_EDITED);

        if (ticket.getManager() != null) {
            eventPublisher.publishEvent(
                    new TicketUpdateEvent(this, ticket.getManager().getEmail(), ticket, userDetails.getUsername(), "내역", userDetails.getUser().getRole())
            );
        }
    }

    // 담당자 개별 수정 - 티켓 유형, 우선순위, 상태, 담당자, 마감기한, 카테고리
    @Transactional
    public void editTypeForManager(Long ticketId, Long typeId, CustomUserDetails userDetails){
        log.info("[담당자 티켓 유형 수정] 요청자: {}, 티켓 ID: {}, 티켓 유형 ID: {}", userDetails.getUsername(), ticketId, typeId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        TicketType ticketType = typeId != null
                ? ticketTypeRepository.findById(typeId).orElseThrow(TicketTypeNotFoundException::new)
                : ticket.getTicketType();

        ticket.updateType(ticketType);

        User user = userDetails.getUser();
        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.TYPE_CHANGE);

        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "유형", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editManager(Long ticketId, Long managerId, CustomUserDetails userDetails){
        log.info("[담당자 티켓 담당자 수정] 요청자: {}, 티켓 ID: {}, 담당자 ID: {}", userDetails.getUsername(), ticketId, managerId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        User manager = userRepository.findById(managerId)
                .orElseThrow(UserNotFoundException::new);

        User user = userDetails.getUser();

        ticket.updateManager(manager);

        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.MANAGER_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "담당자", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editCategoryForManager(EditCategory editCategory, Long ticketId, CustomUserDetails userDetails) {
        log.info("[담당자 티켓 카테고리 수정] 요청자: {}, 티켓 ID: {}, 1차/2차 카테고리 ID: {}/{}",
                userDetails.getUsername(), ticketId, editCategory.getFirstCategoryId(), editCategory.getSecondCategoryId());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        EditCategory category = editCategory;

        validateCategoryRelation(category.getFirstCategoryId(), category.getSecondCategoryId());

        Long firstCategoryId = editCategory.getFirstCategoryId();
        Long secondCategoryId = editCategory.getSecondCategoryId();

        Category firstCategory = firstCategoryId != null
                ? categoryRepository.findById(firstCategoryId).orElseThrow(CategoryNotFoundException::new)
                : null;

        Category secondCategory = secondCategoryId != null
                ? categoryRepository.findById(secondCategoryId).orElseThrow(CategoryNotFoundException::new)
                : null;
        ticket.updateCategory(firstCategory, secondCategory);

        User user = userDetails.getUser();

        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.CATEGORY_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "카테고리", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editDeadlineForManager(Long ticketId, EditSettingRequest editSettingRequest, CustomUserDetails userDetails) {
        log.info("[담당자 티켓 마감기한 수정] 요청자: {}, 티켓 ID: {}, 마감기한: {}", userDetails.getUsername(), ticketId, editSettingRequest.getDeadline());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        ticket.updateDaedlineForManager(editSettingRequest.getDeadline());

        User user = userDetails.getUser();

        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.DEADLINE_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "마감기한", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editPriority(Long ticketId, Ticket.Priority priority, CustomUserDetails userDetails) {
        log.info("[담당자 티켓 우선순위 수정] 요청자: {}, 티켓 ID: {}, 우선순위: {}", userDetails.getUsername(), ticketId, priority);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        ticket.updatePriority(priority);

        User user = userDetails.getUser();

        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.PRIORITY_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "우선순위", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editStatus(Long ticketId, Ticket.Status status, CustomUserDetails userDetails) {
        log.info("[담당자 티켓 상태 수정] 요청자: {}, 티켓 ID: {}, 상태: {}", userDetails.getUsername(), ticketId, status);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        ticket.updateStatus(status);

        User user = userDetails.getUser();

        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.STATUS_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "상태", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editUrgent(Long ticketId,EditTicketRequest editTicketRequest,CustomUserDetails userDetails){
        Boolean urgent = editTicketRequest.getUrgent();
        log.info("[담당자 티켓 긴급상태 수정] 요청자: {}, 티켓 ID: {}, 상태: {}", userDetails.getUsername(), ticketId, urgent);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        ticket.updateUrgent(editTicketRequest);

        User user = userDetails.getUser();
        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.URGENT_CHANGE);
    }

    @Transactional
    public void approveTicket(Long ticketId, CustomUserDetails userDetails) {
        log.info("[담당자 티켓 승인] 요청자: {}, 티켓 ID: {}", userDetails.getUsername(), ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        User manager = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ticket.updateStatus(Ticket.Status.IN_PROGRESS);

        if (ticket.getManager() == null) {
            log.info("[담당자 티켓 승인] 담당자 지정되지 않은 티켓 - 담당자 자동 지정");
            ticket.updateManager(manager);
        }

        historyService.recordHistory(ticket, manager, TicketHistory.UpdateType.STATUS_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "상태", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void rejectTicket(Long ticketId, CustomUserDetails userDetails){
        log.info("[담당자 티켓 거절] 요청자: {}, 티켓 ID: {}", userDetails.getUsername(), ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        ticket.updateStatus(Ticket.Status.REJECTED);

        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "유형", userDetails.getUser().getRole())
        );
    }


    @Transactional
    public void deleteTicket(Long ticketId, CustomUserDetails userDetails) {
        log.info("[티켓 삭제] 요청자: {}, 티켓 ID: {}", userDetails.getUsername(), ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        User user = userDetails.getUser();
        User requester = userRepository.findById(ticket.getRequester().getId())
                .orElseThrow(UserNotFoundException::new);
        if(user.getUsername().equals(requester.getUsername()) && ticket.getStatus().equals(Ticket.Status.PENDING)) {
            ticketRepository.delete(ticket);
            historyService.recordHistory(ticket, user, TicketHistory.UpdateType.TICKET_DELETE);
        }else {
            log.error("[티켓 삭제] 티켓 삭제 권한 없음");
            throw new UnauthorizedTicketAccessException();
        }
    }

    public PendingTicketResponse getPendingTickets(Long managerId) {
        // 담당자가 본인이고 PENDING 상태인 티켓 수
        int myPendingTicket = ticketRepository.countByManagerAndStatus(managerId, Ticket.Status.PENDING);

        // 담당자가 지정되지 않고 PENDING 상태인 티켓 수
        int unassignedPendingTicket = ticketRepository.countByManagerIsNullAndStatus(Ticket.Status.PENDING);

        // 총 대기 티켓 수 (내 요청 + 그룹 요청)
        int totalPendingTicket = myPendingTicket + unassignedPendingTicket;

        // 긴급 대기 티켓 수 (담당자가 본인 or 지정되지 않고 PENDING & URGENT)
        int urgentPendingTicket = ticketRepository.countUrgentPendingTickets(managerId, Ticket.Status.PENDING);

        return new PendingTicketResponse(myPendingTicket, unassignedPendingTicket, totalPendingTicket, urgentPendingTicket);
    }

    private void validateTicketType(Long ticketTypeId) {
        if (ticketTypeId != null && !ticketTypeRepository.existsById(ticketTypeId)) {
            log.info("[티켓 유형 검증] 존재하지 않는 티켓 유형: {}", ticketTypeId);
            throw new TicketTypeNotFoundException();
        }
    }

    private void validateCategoryRelation(Long firstCategoryId, Long secondCategoryId) {
        Category firstCategory = null;
        Category secondCategory = null;

        if (firstCategoryId != null) {
            firstCategory = categoryRepository.findById(firstCategoryId)
                    .orElseThrow(CategoryNotFoundException::new);
        }

        if (secondCategoryId != null) {
            secondCategory = categoryRepository.findById(secondCategoryId)
                    .orElseThrow(CategoryNotFoundException::new);

            if (!secondCategory.isChildOf(firstCategory)) {
                log.error("[카테고리 관계 검증] 2차 카테고리가 1차 카테고리의 하위 카테고리가 아님");
                throw new InvalidCategoryLevelException();
            }
        }
    }

    private void validateUserExistence(Long userId) {
        if (userId != null && !userRepository.existsById(userId)) {
            log.error("[사용자 검증] 존재하지 않는 사용자: {}", userId);
            throw new UserNotFoundException();
        }
    }

    private Ticket buildTicket(CreateTicketRequest request, User requester, User manager,
                               TicketType ticketType, Category firstCategory, Category secondCategory) {
        return Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .urgent(request.getUrgent() != null ? request.getUrgent() : false)
                .ticketType(ticketType)
                .firstCategory(firstCategory)
                .secondCategory(secondCategory)
                .deadline(request.getDeadline())
                .requester(requester)
                .manager(manager)
                .status(Ticket.Status.PENDING)
                .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private User getUserOrThrowForManager(Long userId) {
        try {
            return getUserOrThrow(userId);
        } catch (UserNotFoundException e) {
            log.error("[담당자 검증] 존재하지 않는 담당자: {}", userId);
            throw new InvalidTicketManagerException();
        }
    }

    private TicketType getTicketTypeOrThrow(Long ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(TicketTypeNotFoundException::new);
    }

    private Category getCategoryOrNull(Long categoryId) {
        if (categoryId == null) return null;

        return categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    private void validateCategoryRelation(Category firstCategory, Category secondCategory) {
        if (secondCategory != null && (firstCategory == null || !secondCategory.isChildOf(firstCategory))) {
            log.error("[카테고리 관계 검증] 2차 카테고리가 1차 카테고리의 하위 카테고리가 아님");
            throw new InvalidCategoryLevelException();
        }
    }
}
