package com.trillion.tikitaka.tickettype.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import com.trillion.tikitaka.tickettype.exception.DuplicatedTicketTypeException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    // 티켓 유형 생성
    @Transactional
    public void createTicketType(String typeName) {
        ticketTypeRepository.findByName(typeName)
                .ifPresent(ticketType -> {
                    throw new DuplicatedTicketTypeException();
                });

        TicketType ticketType = TicketType.builder()
                .name(typeName)
                .build();

        ticketTypeRepository.save(ticketType);
    }

    public List<TicketTypeListResponse> getTicketTypes(Boolean active, CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

        if (!isAdmin) active = true;

        return ticketTypeRepository.getTicketTypes(active);
    }
}
