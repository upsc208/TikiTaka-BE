package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.registration.exception.DuplicatedUsernameException;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.dto.CreateTicketRequest;
import com.trillion.tikitaka.ticket.dto.EditTicketRequest;
import com.trillion.tikitaka.ticket.exception.InvaildTicketManagerException;
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


        Long managerId = (request.getManagerId() == null) ? 3L : request.getManagerId();//담당자가 미지정되거나 값이 null인경우 전체를 뜻하는 3번유저를 선택하도록한다.

        if (!userRepository.existsById(managerId)) {
            throw new InvaildTicketManagerException();
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
                .is_active(true)
                .status(Ticket.Status.PENDING)
                .priority(request.getPriority())
                .build();

        ticketRepository.save(ticket);
    }

    @Transactional
    public void editTicket(EditTicketRequest request, Long ticketId) {
        Ticket oldTicket = this.findTicketById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        Ticket updatedTicket = Ticket.builder()
                .id(oldTicket.getId())
                .title(request.getTitle() != null ? request.getTitle() : oldTicket.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : oldTicket.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : oldTicket.getPriority())
                .status(request.getStatus() != null ? request.getStatus() : oldTicket.getStatus())
                .typeId(request.getTypeId() != null ? request.getTypeId() : oldTicket.getTypeId())
                .firstCategoryId(request.getFirstCategoryId() != null ? request.getFirstCategoryId() : oldTicket.getFirstCategoryId())
                .secondCategoryId(request.getSecondCategoryId() != null ? request.getSecondCategoryId() : oldTicket.getSecondCategoryId())
                .deadline(request.getDeadline() != null ? request.getDeadline() : oldTicket.getDeadline())
                .requesterId(request.getRequesterId() != null ? request.getRequesterId() : oldTicket.getRequesterId())
                .managerId(request.getManagerId() != null ? request.getManagerId() : oldTicket.getManagerId())
                .urgent(request.getUrgent() != null ? request.getUrgent() : oldTicket.getUrgent())
                .build();


        replaceTicket(oldTicket, updatedTicket);
    }


    private void replaceTicket(Ticket oldTicket, Ticket updatedTicket) {

        oldTicket.updateFrom(updatedTicket);
    }


    public void DeleteTicket(Long ticket_id){
        Ticket oldTicket = this.findTicketById(ticket_id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticket_id));
        oldTicket.deactivate();
    }

    public Page<Ticket> getAllTicket(Pageable pageable) {
        //TODO:권한별로 보이는 티켓이 다르게 보이는 기능 구현 필요
        return ticketRepository.findAll(pageable);
    }
    public String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("유효하지 않은 Authorization 헤더입니다.");
        }
        return authorizationHeader.substring(7);
    }

//    public Page<Ticket> getTicketsByRole(String accessToken, Pageable pageable) {
//        // AccessToken에서 Role과 Username 추출
//        String role = jwtUtil.getRole(accessToken);
//        String username = jwtUtil.getUsername(accessToken);
//
//        switch (role) {
//            case "ADMIN":
//                return ticketRepository.findAll(pageable);
//
//            case "MANAGER":
//                return ticketRepository.findByManagerId(username, pageable);
//
//            case "USER":
//                return ticketRepository.findByRequesterId(username, pageable);
//
//            default:
//                throw new RuntimeException("권한이 없습니다.");
//        }
//    }

}
