package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import com.trillion.tikitaka.ticket.exception.InvalidTicketManagerException;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CategoryRepository categoryRepository;

    public Optional<Ticket> findTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public void createTicket(CreateTicketRequest request, Long requesterId) {


        validateTicketType(request.getTypeId());
        validateCategoryRelation(request.getFirstCategoryId(), request.getSecondCategoryId());
        validateUserExistence(requesterId);
        if (request.getManagerId() != null) {
            validateUserExistence(request.getManagerId());
        }


        TicketType ticketType = ticketTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new DuplicatedTicketTypeException());

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
    }



    @Transactional
    public void editTicket(EditTicketRequest request, Long ticketId) {

        Ticket ticket = findTicketById(ticketId)
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
    public void editSetting(Long ticketId, Role role, EditSettingRequest editSettingRequest){
        Ticket ticket = this.findTicketById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());

        if (Role.USER.equals(role)) {
            throw new UnauthorizedTicketEditExeception();
        }else{
            ticket.updateSetting(editSettingRequest);
        }
    }
    @Transactional
    public void editStatus(Long ticketId, Role role, Ticket.Status status){
        Ticket ticket = this.findTicketById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        if (Role.USER.equals(role)) {
            throw new UnauthorizedTicketEditExeception();
        }else{
            ticket.updateStatus(status);
        }
    }
    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
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
