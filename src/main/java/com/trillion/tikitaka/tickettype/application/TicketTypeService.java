package com.trillion.tikitaka.tickettype.application;

import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import com.trillion.tikitaka.tickettype.exception.DuplicatedTicketTypeException;
import com.trillion.tikitaka.tickettype.exception.TicketTypeNotFoundException;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public void createTicketType(String typeName) {
        log.info("[티켓 타입 생성] 타입명: {}", typeName);
        ticketTypeRepository.findByName(typeName)
                .ifPresent(ticketType -> {
                    log.error("[티켓 타입 생성] 중복된 타입명");
                    throw new DuplicatedTicketTypeException();
                });

        TicketType ticketType = TicketType.builder()
                .name(typeName)
                .build();

        ticketTypeRepository.save(ticketType);
    }

    public List<TicketTypeListResponse> getTicketTypes() {
        log.info("[티켓 타입 조회]");
        return ticketTypeRepository.getTicketTypes();
    }

    @Transactional
    public void updateTicketType(Long typeId, String typeName) {
        log.info("[티켓 타입 수정] 타입 ID: {}, 타입명: {}", typeId, typeName);
        List<TicketType> ticketTypes = ticketTypeRepository.findByIdAndNameCheck(typeId, typeName);

        boolean idExists = ticketTypes.stream()
                .anyMatch(ticketType -> ticketType.getId().equals(typeId));

        if (!idExists) {
            log.error("[티켓 타입 수정] 타입 ID가 존재하지 않음");
            throw new TicketTypeNotFoundException();
        }

        boolean nameExists = ticketTypes.stream()
                .anyMatch(ticketType -> ticketType.getName().equals(typeName) && !ticketType.getId().equals(typeId));

        if (nameExists) {
            log.error("[티켓 타입 수정] 중복된 타입명");
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
        log.info("[티켓 타입 삭제] 타입 ID: {}", typeId);
        TicketType ticketType = ticketTypeRepository.findById(typeId)
                .orElseThrow(TicketTypeNotFoundException::new);
        if(ticketType.isDefaultType()){
            throw new TransactionSystemException("기본 티켓 유형은 변경할 수 없습니다.");
        }
        ticketTypeRepository.delete(ticketType);
    }
}
