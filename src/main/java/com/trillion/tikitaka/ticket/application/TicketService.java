package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.exception.DuplicatedUsernameException;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditSettingRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import com.trillion.tikitaka.ticket.exception.InvalidEditValueException;
import com.trillion.tikitaka.ticket.exception.InvalidTicketManagerException;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.exception.UnauthorizedStatusEditExeception;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
//import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


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
                    .priority(oldTicket.getPriority())
                    .ticketType(ticketType != null ? ticketType : oldTicket.getTicketType())
                    .firstCategoryId(request.getFirstCategoryId() != null ? request.getFirstCategoryId() : oldTicket.getFirstCategoryId())
                    .secondCategoryId(request.getSecondCategoryId() != null ? request.getSecondCategoryId() : oldTicket.getSecondCategoryId())
                    .deadline(request.getDeadline() != null ? request.getDeadline() : oldTicket.getDeadline())
                    .requesterId(request.getRequesterId() != null ? request.getRequesterId() : oldTicket.getRequesterId())
                    .managerId(oldTicket.getManagerId())
                    .urgent(request.getUrgent() != null ? request.getUrgent() : oldTicket.getUrgent())
                    .build();

            replaceTicket(oldTicket, updatedTicket);
    }


    private void replaceTicket(Ticket oldTicket, Ticket updatedTicket) {

        oldTicket.updateFrom(updatedTicket);
    }
    public void editSetting(Long ticketId, String role, EditSettingRequest editSettingRequest){

    }
    public void editStatus(Long ticketId,String role, Ticket.Status status){
        Ticket oldTicket = this.findTicketById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        if ("USER".equals(role)) {
            throw new UnauthorizedStatusEditExeception();
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
