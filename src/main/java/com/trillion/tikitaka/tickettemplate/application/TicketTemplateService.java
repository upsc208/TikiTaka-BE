package com.trillion.tikitaka.tickettemplate.application;

import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateInvalidFKException;
import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
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
        if (!typeRepository.existsById(req.getTypeId())
                || !categoryRepository.existsById(req.getFirstCategoryId())
                || !categoryRepository.existsById(req.getSecondCategoryId())
                || !userRepository.existsById(req.getRequesterId())) {
            throw new TicketTemplateInvalidFKException();
        }

        TicketTemplate entity = TicketTemplate.builder()
                .templateTitle(req.getTemplateTitle())
                .title(req.getTitle())
                .description(req.getDescription())
                .typeId(req.getTypeId())
                .firstCategoryId(req.getFirstCategoryId())
                .secondCategoryId(req.getSecondCategoryId())
                .requesterId(req.getRequesterId())
                .managerId(req.getManagerId()) // optional
                .build();

        return templateRepository.save(entity).getId();
    }


    @Transactional
    public void updateTicketTemplate(Long id, TicketTemplateRequest req) {

        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("티켓 템플릿이 존재하지 않습니다. id=" + id));


        if (!typeRepository.existsById(req.getTypeId())
                || !categoryRepository.existsById(req.getFirstCategoryId())
                || !categoryRepository.existsById(req.getSecondCategoryId())
                || !userRepository.existsById(req.getRequesterId())) {
            throw new TicketTemplateInvalidFKException();
        }


        template.update(
                req.getTemplateTitle(),
                req.getTitle(),
                req.getDescription(),
                req.getTypeId(),
                req.getFirstCategoryId(),
                req.getSecondCategoryId(),
                req.getRequesterId(),
                req.getManagerId()
        );
    }
}
