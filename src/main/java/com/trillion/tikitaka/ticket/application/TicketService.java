package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.ticket.dto.response.TicketCountByStatusResponse;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;

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
        Boolean isUser = userDetails.getAuthorities().contains("USER");
        return ticketRepository.countTicketsByStatus(isUser, userDetails.getUser().getId());
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
