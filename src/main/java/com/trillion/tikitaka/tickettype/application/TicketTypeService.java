package com.trillion.tikitaka.tickettype.application;

import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import com.trillion.tikitaka.tickettype.exception.DuplicatedTicketTypeException;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
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

    public List<TicketTypeListResponse> getTicketTypes() {

        return ticketTypeRepository.getTicketTypes();
    }

    @Transactional
    public void updateTicketType(Long typeId, String typeName) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByIdAndNameCheck(typeId, typeName);

        boolean idExists = ticketTypes.stream()
                .anyMatch(ticketType -> ticketType.getId().equals(typeId));

        if (!idExists) {
            throw new TicketTypeNotFoundException();
        }

        boolean nameExists = ticketTypes.stream()
                .anyMatch(ticketType -> ticketType.getName().equals(typeName) && !ticketType.getId().equals(typeId));

        if (nameExists) {
            throw new DuplicatedTicketTypeException();
        }

        TicketType ticketType = ticketTypes.stream()
                .filter(t -> t.getId().equals(typeId))
                .findFirst()
                .orElseThrow(TicketTypeNotFoundException::new);

        ticketType.updateName(typeName);
    }

    @Transactional
    public void deleteTicketType(Long typeId) {
        TicketType ticketType = ticketTypeRepository.findById(typeId)
                .orElseThrow(TicketTypeNotFoundException::new);

        ticketTypeRepository.delete(ticketType);
    }
}
