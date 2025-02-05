package com.trillion.tikitaka.tickettemplate.application;

import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse; // 추가
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateInvalidFKException;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateNotFoundException;
import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTemplateService {

    private final TicketTemplateRepository templateRepository;
    private final TicketTypeRepository typeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createTicketTemplate(TicketTemplateRequest req) {
        // [생성 로직: 변경 없음]
        if (!typeRepository.existsById(req.getTypeId())
                || !categoryRepository.existsById(req.getFirstCategoryId())
                || !categoryRepository.existsById(req.getSecondCategoryId())) {
            throw new TicketTemplateInvalidFKException();
        }
        User requester = userRepository.findById(req.getRequesterId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(TicketTemplateInvalidFKException::new);
        }

        TicketTemplate entity = TicketTemplate.builder()
                .templateTitle(req.getTemplateTitle())
                .title(req.getTitle())
                .description(req.getDescription())
                .typeId(req.getTypeId())
                .firstCategoryId(req.getFirstCategoryId())
                .secondCategoryId(req.getSecondCategoryId())
                .requester(requester)
                .manager(manager)
                .build();

        return templateRepository.save(entity).getId();
    }

    @Transactional
    public void updateTicketTemplate(Long id, TicketTemplateRequest req) {
        // [수정 로직: 변경 없음]
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        if (!typeRepository.existsById(req.getTypeId())
                || !categoryRepository.existsById(req.getFirstCategoryId())
                || !categoryRepository.existsById(req.getSecondCategoryId())) {
            throw new TicketTemplateInvalidFKException();
        }

        User requester = userRepository.findById(req.getRequesterId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(TicketTemplateInvalidFKException::new);
        }

        template.update(
                req.getTemplateTitle(),
                req.getTitle(),
                req.getDescription(),
                req.getTypeId(),
                req.getFirstCategoryId(),
                req.getSecondCategoryId(),
                requester,
                manager
        );
    }

    @Transactional
    public void deleteTicketTemplate(Long id) {
        // [삭제 로직: 변경 없음]
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);
        templateRepository.delete(template);
    }

    // [신규] 단일 조회
    public TicketTemplateResponse getOneTicketTemplate(Long id) {
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        // requester, manager가 User 엔티티
        // -> 응답은 "requesterId", "managerId"만. manager가 null이면 null
        Long requesterId = template.getRequester().getId();
        Long managerId = (template.getManager() != null) ? template.getManager().getId() : null;

        // DTO 생성
        return new TicketTemplateResponse(
                template.getTemplateTitle(),
                template.getTitle(),
                template.getDescription(),
                template.getTypeId(),
                template.getFirstCategoryId(),
                template.getSecondCategoryId(),
                requesterId,
                managerId
        );
    }
}
