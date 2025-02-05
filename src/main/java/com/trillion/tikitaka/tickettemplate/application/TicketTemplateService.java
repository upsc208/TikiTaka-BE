package com.trillion.tikitaka.tickettemplate.application;

import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateInvalidFKException;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateNotFoundException;
import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;

import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;        // 예: we assume there's a TicketType entity with getName()
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.category.domain.Category;           // assume a Category entity with getName()
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
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        templateRepository.delete(template);
    }

    public TicketTemplateResponse getOneTicketTemplate(Long id) {
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        TicketType typeEntity = typeRepository.findById(template.getTypeId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        String typeName = typeEntity.getName();

        Category firstCat = categoryRepository.findById(template.getFirstCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        String firstCategoryName = firstCat.getName();

        Category secondCat = categoryRepository.findById(template.getSecondCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        String secondCategoryName = secondCat.getName();

        User requester = template.getRequester();
        Long requesterId = requester.getId();
        String requesterName = requester.getUsername();

        User manager = template.getManager();
        Long managerId = null;
        String managerName = null;
        if (manager != null) {
            managerId = manager.getId();
            managerName = manager.getUsername();
        }

        return new TicketTemplateResponse(
                template.getTemplateTitle(),
                template.getTitle(),
                template.getDescription(),
                template.getTypeId(),
                typeName,
                template.getFirstCategoryId(),
                firstCategoryName,
                template.getSecondCategoryId(),
                secondCategoryName,
                requesterId,
                requesterName,
                managerId,
                managerName
        );
    }
}
