package com.trillion.tikitaka.ticket.application;


import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.notification.domain.NotificationType;
import com.trillion.tikitaka.notification.event.TicketCreationEvent;
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
import com.trillion.tikitaka.ticket.exception.UnauthorizedTicketEditExeception;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.exception.DuplicatedTicketTypeException;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createTicket(CreateTicketRequest request, Long requesterId) {
        validateTicketType(request.getTypeId());
        validateCategoryRelation(request.getFirstCategoryId(), request.getSecondCategoryId());
        validateUserExistence(requesterId);
        if (request.getManagerId() != null) {
            validateUserExistence(request.getManagerId());
        }

        TicketType ticketType = ticketTypeRepository.findById(request.getTypeId())
                .orElseThrow(DuplicatedTicketTypeException::new);

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Requester ID: " + requesterId));

        User manager = request.getManagerId() != null ? userRepository.findById(request.getManagerId())
                .orElseThrow(InvalidTicketManagerException::new) : null;

        Category firstCategory = request.getFirstCategoryId() != null ?
                categoryRepository.findById(request.getFirstCategoryId())
                        .orElseThrow(CategoryNotFoundException::new) : null;

        Category secondCategory = request.getSecondCategoryId() != null ?
                categoryRepository.findById(request.getSecondCategoryId())
                        .orElseThrow(CategoryNotFoundException::new) : null;

        Ticket ticket = Ticket.builder()
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

        ticketRepository.save(ticket);

        if (ticket.getManager() != null){
            eventPublisher.publishEvent(
                    new TicketCreationEvent(this, ticket.getManager().getEmail(), ticket, NotificationType.TICKET_CREATION)
            );
        } else {
            List<User> managers = userRepository.findAllByRole(Role.MANAGER);
            for (User m : managers) {
                eventPublisher.publishEvent(
                        new TicketCreationEvent(this, m.getEmail(), ticket, NotificationType.TICKET_CREATION)
                );
            }
        }
    }

    public TicketCountByStatusResponse countTicketsByStatus(CustomUserDetails userDetails) {
        Optional<? extends GrantedAuthority> roleOpt = userDetails.getAuthorities().stream().findFirst();
        String role = roleOpt.map(GrantedAuthority::getAuthority).orElse(null);
        Long requesterId = "USER".equals(role) ? userDetails.getUser().getId() : null;

        return ticketRepository.countTicketsByStatus(requesterId, role);
    }

    public Page<TicketListResponse> getTicketList(Pageable pageable, Ticket.Status status, Long firstCategoryId,
                                                  Long secondCategoryId, Long ticketTypeId, Long managerId, Long requesterId,
                                                  CustomUserDetails userDetails) {
        Optional<? extends GrantedAuthority> roleOpt = userDetails.getAuthorities().stream().findFirst();
        String role = roleOpt.map(GrantedAuthority::getAuthority).orElse(null);

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
        Optional<? extends GrantedAuthority> roleOpt = userDetails.getAuthorities().stream().findFirst();
        String role = roleOpt.map(GrantedAuthority::getAuthority).orElse(null);
        Long userId = userDetails.getUser().getId();

        TicketResponse response = ticketRepository.getTicket(ticketId, userId, role);
        if (response == null) throw new TicketNotFoundException();

        if ("USER".equals(role)) {
            response.setPriority(null);
        }

        return response;
    }

    @Transactional
    public void editTicket(EditTicketRequest request, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        validateTicketType(request.getTicketTypeId());
        TicketType ticketType = request.getTicketTypeId() != null
                ? ticketTypeRepository.findById(request.getTicketTypeId()).orElseThrow(TicketTypeNotFoundException::new)
                : ticket.getTicketType();

        validateCategoryRelation(request.getFirstCategoryId(), request.getSecondCategoryId());

        Category firstCategory = request.getFirstCategoryId() != null
                ? categoryRepository.findById(request.getFirstCategoryId()).orElseThrow(CategoryNotFoundException::new)
                : null;

        Category secondCategory = request.getSecondCategoryId() != null
                ? categoryRepository.findById(request.getSecondCategoryId()).orElseThrow(CategoryNotFoundException::new)
                : null;

        ticket.update(request, ticketType, firstCategory, secondCategory);
    }

    @Transactional
    public void editSetting(Long ticketId, Role role, EditSettingRequest editSettingRequest) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        if (Role.USER.equals(role)) {
            throw new UnauthorizedTicketEditExeception();
        } else {
            ticket.updateSetting(editSettingRequest);
        }
    }

    @Transactional
    public void editStatus(Long ticketId, Role role, Ticket.Status status){
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        if (Role.USER.equals(role)) {
            throw new UnauthorizedTicketEditExeception();
        }else{
            ticket.updateStatus(status);
        }
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        ticketRepository.delete(ticket);
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

}
