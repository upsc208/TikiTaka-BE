package com.trillion.tikitaka.ticket.application;


import com.trillion.tikitaka.attachment.application.FileService;
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
import com.trillion.tikitaka.ticket.dto.request.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.request.EditTicketRequest;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final HistoryService historyService;
    private final FileService fileService;

    @Transactional
    public void createTicket(CreateTicketRequest request, List<MultipartFile> files, CustomUserDetails userDetails) {
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
            fileService.uploadFiles(files, ticket);
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
    }

    public TicketCountByStatusResponse countTicketsByStatus(CustomUserDetails userDetails) {
        String role = userDetails.getUser().getRole().toString();
        Long requesterId = "USER".equals(role) ? userDetails.getUser().getId() : null;

        return ticketRepository.countTicketsByStatus(requesterId, role);
    }

    public Page<TicketListResponse> getTicketList(Pageable pageable, Ticket.Status status, Long firstCategoryId,
                                                  Long secondCategoryId, Long ticketTypeId, Long managerId, Long requesterId,
                                                  CustomUserDetails userDetails) {
        String role = userDetails.getUser().getRole().toString();

        if ("USER".equals(role)) {
            requesterId = userDetails.getUser().getId();
            if (managerId != null) {
                throw new UnauthorizedTicketAccessException();
            }
        }

        validateTicketType(ticketTypeId);
        validateCategoryRelation(firstCategoryId, secondCategoryId);
        validateUserExistence(requesterId);
        validateUserExistence(managerId);

        return ticketRepository.getTicketList(pageable, status, firstCategoryId, secondCategoryId, ticketTypeId, managerId, requesterId, role);
    }

    public TicketResponse getTicket(Long ticketId, CustomUserDetails userDetails) {
        String role = userDetails.getUser().getRole().toString();
        Long userId = userDetails.getUser().getId();

        TicketResponse response = ticketRepository.getTicket(ticketId, userId, role);
        if (response == null) throw new TicketNotFoundException();

        if ("USER".equals(role)) {
            response.setPriority(null);
        }

        return response;
    }

    @Transactional
    public void editTicket(EditTicketRequest request, Long ticketId, CustomUserDetails userDetails) {
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
    public void editCategoryForManager(Long firstCategoryId, Long secondCategoryId, Long ticketId, CustomUserDetails userDetails){
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        validateCategoryRelation(firstCategoryId, secondCategoryId);

        Category firstCategory = firstCategoryId != null
                ? categoryRepository.findById(firstCategoryId).orElseThrow(CategoryNotFoundException::new)
                : null;

        Category secondCategory = secondCategoryId != null
                ? categoryRepository.findById(secondCategoryId).orElseThrow(CategoryNotFoundException::new)
                : null;
        ticket.updateCategory(firstCategory,secondCategory);

        User user = userDetails.getUser();

        historyService.recordHistory(ticket, user, TicketHistory.UpdateType.CATEGORY_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "카테고리", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void editDeadlineForManager(Long ticketId, EditSettingRequest editSettingRequest, CustomUserDetails userDetails){
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
    public void editPriority(Long ticketId, Ticket.Priority priority, CustomUserDetails userDetails){
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
    public void editStatus(Long ticketId, Ticket.Status status, CustomUserDetails userDetails){
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
    public void approveTicket(Long ticketId, CustomUserDetails userDetails){
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        User manager = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ticket.updateStatus(Ticket.Status.IN_PROGRESS);

        if(ticket.getManager() == null){
            ticket.updateManager(manager);
        }

        historyService.recordHistory(ticket, manager, TicketHistory.UpdateType.STATUS_CHANGE);
        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "상태", userDetails.getUser().getRole())
        );
    }

    @Transactional
    public void rejectTicket(Long ticketId, CustomUserDetails userDetails){
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        ticket.updateStatus(Ticket.Status.REJECTED);

        eventPublisher.publishEvent(
                new TicketUpdateEvent(this, ticket.getRequester().getEmail(), ticket, userDetails.getUsername(), "유형", userDetails.getUser().getRole())
        );
    }


    @Transactional
    public void deleteTicket(Long ticketId, CustomUserDetails userDetails) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        User user = userDetails.getUser();
        User requester = userRepository.findById(ticket.getRequester().getId())
                .orElseThrow(UserNotFoundException::new);
        if(user.getUsername().equals(requester.getUsername()) && ticket.getStatus().equals(Ticket.Status.PENDING)) {
            ticketRepository.delete(ticket);
            historyService.recordHistory(ticket, user, TicketHistory.UpdateType.TICKET_DELETE);
        }else{ throw new UnauthorizedTicketAccessException();}

    }

    private void validateTicketType(Long ticketTypeId) {
        if (ticketTypeId != null && !ticketTypeRepository.existsById(ticketTypeId)) {
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
                throw new InvalidCategoryLevelException();
            }
        }
    }

    private void validateUserExistence(Long userId) {
        if (userId != null && !userRepository.existsById(userId)) {
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
            throw new InvalidCategoryLevelException();
        }
    }
}
