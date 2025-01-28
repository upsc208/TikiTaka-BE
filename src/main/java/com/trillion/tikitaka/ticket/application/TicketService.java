package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import com.trillion.tikitaka.ticket.exception.InvalidTicketManagerException;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.exception.UnauthorizedTicketEditExeception;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
//import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final JwtUtil jwtUtil;

    public Optional<Ticket> findTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public void createTicket(CreateTicketRequest request) {


        TicketType ticketType = ticketTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid TicketType ID: " + request.getTypeId()));
        Long managerId = request.getManagerId();

        if (managerId != null && !userRepository.existsById(managerId)) {
            throw new InvalidTicketManagerException();
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .urgent(request.getUrgent() != null ? request.getUrgent() : false)
                .ticketType(ticketType)
                .firstCategoryId(request.getFirstCategoryId())
                .secondCategoryId(request.getSecondCategoryId())
                .deadline(request.getDeadline())
                .requesterId(request.getRequesterId())
                .managerId(managerId)
                .status(Ticket.Status.PENDING)
                .build();

        ticketRepository.save(ticket);
    }

    @Transactional
    public void editTicket(EditTicketRequest request, Long ticketId) {
        Ticket oldTicket = this.findTicketById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        TicketType ticketType = null;
        if (request.getTicketTypeId() != null) {
            ticketType = ticketTypeRepository.findById(request.getTicketTypeId())
                    .orElseThrow(() -> new TicketTypeNotFoundException());
        }
            Ticket updatedTicket = Ticket.builder()
                    .id(oldTicket.getId())
                    .title(request.getTitle() != null ? request.getTitle() : oldTicket.getTitle())
                    .description(request.getDescription() != null ? request.getDescription() : oldTicket.getDescription())
                    .ticketType(ticketType != null ? ticketType : oldTicket.getTicketType())
                    .firstCategoryId(request.getFirstCategoryId() != null ? request.getFirstCategoryId() : oldTicket.getFirstCategoryId())
                    .secondCategoryId(request.getSecondCategoryId() != null ? request.getSecondCategoryId() : oldTicket.getSecondCategoryId())
                    .urgent(request.getUrgent() != null ? request.getUrgent() : oldTicket.getUrgent())
                    .status(oldTicket.getStatus())
                    .requesterId(oldTicket.getRequesterId())
                    .managerId(oldTicket.getManagerId())
                    .deadline(oldTicket.getDeadline())
                    .priority(oldTicket.getPriority())
                    .build();

            replaceTicket(oldTicket, updatedTicket);
    }


    private void replaceTicket(Ticket oldTicket, Ticket updatedTicket) {

        oldTicket.updateFrom(updatedTicket);
    }
    @Transactional
    public void editSetting(Long ticketId, Role role, EditSettingRequest editSettingRequest){
        Ticket oldTicket = this.findTicketById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        System.out.println(role);
        if (Role.USER.equals(role)) {
            throw new UnauthorizedTicketEditExeception();
        }else{
            Ticket updatedTicket = Ticket.builder()
                    .id(oldTicket.getId())
                    .title(oldTicket.getTitle())
                    .description(oldTicket.getDescription())
                    .ticketType(oldTicket.getTicketType())
                    .firstCategoryId(oldTicket.getFirstCategoryId())
                    .secondCategoryId(oldTicket.getSecondCategoryId())
                    .urgent(oldTicket.getUrgent())
                    .status(oldTicket.getStatus())
                    .requesterId(oldTicket.getRequesterId())
                    .priority(editSettingRequest.getPriority() != null ? editSettingRequest.getPriority() : oldTicket.getPriority())
                    .managerId(editSettingRequest.getManagerId() != null ? editSettingRequest.getManagerId() : oldTicket.getManagerId())
                    .deadline(editSettingRequest.getDeadline() != null ? editSettingRequest.getDeadline() : oldTicket.getDeadline())
                    .build();

            replaceTicket(oldTicket, updatedTicket);
        }
    }
    @Transactional
    public void editStatus(Long ticketId, Role role, Ticket.Status status){
        Ticket oldTicket = this.findTicketById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        if (Role.USER.equals(role)) {
            throw new UnauthorizedTicketEditExeception();
        }else{
            oldTicket.setStatus(status);
        }
    }
    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        ticketRepository.delete(ticket);
    }


}
