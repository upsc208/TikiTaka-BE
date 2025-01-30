package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
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

    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;

    public Optional<Ticket> findTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public void createTicket(CreateTicketRequest request, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(UserNotFoundException::new);

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(UserNotFoundException::new);
            manager = (manager.getRole().equals(Role.MANAGER)) ? manager : null;
        }

        TicketType ticketType = ticketTypeRepository.findById(request.getTypeId())
                .orElseThrow(TicketTypeNotFoundException::new);

        Category firstCategory = (request.getFirstCategoryId() != null) ?
                categoryRepository.findById(request.getFirstCategoryId())
                        .orElseThrow(CategoryNotFoundException::new) : null;

        Category secondCategory = (request.getSecondCategoryId() != null) ?
                categoryRepository.findById(request.getSecondCategoryId())
                        .orElseThrow(CategoryNotFoundException::new) : null;

        if (firstCategory != null && secondCategory != null) {
            if (!secondCategory.isChildOf(firstCategory)) {
                throw new InvalidCategoryLevelException();
            }
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .ticketType(ticketType)
                .firstCategory(firstCategory)
                .secondCategory(secondCategory)
                .deadline(request.getDeadline())
                .requester(requester)
                .manager(manager)
                .urgent(request.getUrgent())
                .build();
        ticketRepository.save(ticket);

        // TODO: 담당자에게 알림 전송
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
}
