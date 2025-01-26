package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.exception.DuplicatedUsernameException;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import com.trillion.tikitaka.ticket.exception.InvalidTicketManagerException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
//import com.trillion.tikitaka.authentication.application.util.JwtUtil;
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
    //private final JwtUtil jwtUtil;

    public Optional<Ticket> findTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public void createTicket(CreateTicketRequest request) {


        Long managerId = request.getManagerId();

        if (managerId != null && !userRepository.existsById(managerId)) {
            throw new InvalidTicketManagerException();
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .urgent(request.getUrgent() != null ? request.getUrgent() : false)
                .typeId(request.getTypeId())
                .firstCategoryId(request.getFirstCategoryId())
                .secondCategoryId(request.getSecondCategoryId())
                .deadline(request.getDeadline())
                .requesterId(request.getRequesterId())
                .managerId(managerId)
                .status(Ticket.Status.PENDING)
                .priority(request.getPriority())
                .build();

        ticketRepository.save(ticket);
    }


}
