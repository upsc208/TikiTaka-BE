package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.dto.response.TicketListResponse;
import com.trillion.tikitaka.ticket.exception.UnauthorizedTicketAccessException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CategoryRepository categoryRepository;

//    public Optional<Ticket> findTicketById(Long id) {
//        return ticketRepository.findById(id);
//    }
//
//    @Transactional
//    public void createTicket(CreateTicketRequest request) {
//
//
//        TicketType ticketType = ticketTypeRepository.findById(request.getTypeId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid TicketType ID: " + request.getTypeId()));
//        Long managerId = request.getManagerId();
//
//        if (managerId != null && !userRepository.existsById(managerId)) {
//            throw new InvalidTicketManagerException();
//        }
//
//        Ticket ticket = Ticket.builder()
//                .title(request.getTitle())
//                .description(request.getDescription())
//                .urgent(request.getUrgent() != null ? request.getUrgent() : false)
//                .ticketType(ticketType)
//                .firstCategoryId(request.getFirstCategoryId())
//                .secondCategoryId(request.getSecondCategoryId())
//                .deadline(request.getDeadline())
//                .requesterId(request.getRequesterId())
//                .managerId(managerId)
//                .status(Ticket.Status.PENDING)
//                .build();
//
//        ticketRepository.save(ticket);
//    }

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

        return ticketRepository.getTicketList(
                pageable, status, firstCategoryId, secondCategoryId, ticketTypeId, managerId, requesterId, role);
    }

//    @Transactional
//    public void editTicket(EditTicketRequest request, Long ticketId) {
//        Ticket ticket = this.findTicketById(ticketId)
//                .orElseThrow(() -> new TicketNotFoundException());
//
//        TicketType ticketType = request.getTicketTypeId() != null
//                ? ticketTypeRepository.findById(request.getTicketTypeId()).orElseThrow(TicketTypeNotFoundException::new)
//                : ticket.getTicketType();
//        ticket.update(request, ticketType);
//
//    }
//
//
//    @Transactional
//    public void editSetting(Long ticketId, Role role, EditSettingRequest editSettingRequest){
//        Ticket ticket = this.findTicketById(ticketId)
//                .orElseThrow(() -> new TicketNotFoundException());
//        System.out.println(role);
//        if (Role.USER.equals(role)) {
//            throw new UnauthorizedTicketEditExeception();
//        }else{
//            ticket.updateSetting(editSettingRequest);
//        }
//    }
//    @Transactional
//    public void editStatus(Long ticketId, Role role, Ticket.Status status){
//        Ticket ticket = this.findTicketById(ticketId)
//                .orElseThrow(() -> new TicketNotFoundException());
//        if (Role.USER.equals(role)) {
//            throw new UnauthorizedTicketEditExeception();
//        }else{
//            ticket.updateStatus(status);
//        }
//    }
//    @Transactional
//    public void deleteTicket(Long ticketId) {
//        Ticket ticket = ticketRepository.findById(ticketId)
//                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
//        ticketRepository.delete(ticket);
//    }

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
