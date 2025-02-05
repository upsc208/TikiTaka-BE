package com.trillion.tikitaka.tickettemplate.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.tickettemplate.domain.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateListResponse;
import com.trillion.tikitaka.tickettemplate.dto.response.TicketTemplateResponse;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateInvalidFKException;
import com.trillion.tikitaka.tickettemplate.exception.TicketTemplateNotFoundException;
import com.trillion.tikitaka.tickettemplate.infrastructure.TicketTemplateRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTemplateService {

    private final TicketTemplateRepository templateRepository;
    private final TicketTypeRepository typeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public Long createTicketTemplate(TicketTemplateRequest req) {
        TicketType type = typeRepository.findById(req.getTypeId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category firstCat = categoryRepository.findById(req.getFirstCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category secondCat = categoryRepository.findById(req.getSecondCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);

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
                .type(type)
                .firstCategory(firstCat)
                .secondCategory(secondCat)
                .requester(requester)
                .manager(manager)
                .build();

        return templateRepository.save(entity).getId();
    }

    @Transactional
    public void updateTicketTemplate(Long id, TicketTemplateRequest req) {
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        TicketType type = typeRepository.findById(req.getTypeId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category firstCat = categoryRepository.findById(req.getFirstCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category secondCat = categoryRepository.findById(req.getSecondCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);

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
                type,
                firstCat,
                secondCat,
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

        TicketType typeEntity = template.getType();
        Long typeId = typeEntity.getId();
        String typeName = typeEntity.getName();

        Category firstCat = template.getFirstCategory();
        Long firstCatId = firstCat.getId();
        String firstCatName = firstCat.getName();

        Category secondCat = template.getSecondCategory();
        Long secondCatId = secondCat.getId();
        String secondCatName = secondCat.getName();

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
                typeId,
                typeName,
                firstCatId,
                firstCatName,
                secondCatId,
                secondCatName,
                requesterId,
                requesterName,
                managerId,
                managerName
        );
    }

    public List<TicketTemplateListResponse> getAllTicketTemplates() {
        List<TicketTemplate> templates = templateRepository.findAll();

        return templates.stream()
                .map(template -> {
                    TicketType typeEntity = template.getType();
                    Long typeId = typeEntity.getId();
                    String typeName = typeEntity.getName();

                    Category firstCat = template.getFirstCategory();
                    Long firstCatId = firstCat.getId();
                    String firstCatName = firstCat.getName();

                    Category secondCat = template.getSecondCategory();
                    Long secondCatId = secondCat.getId();
                    String secondCatName = secondCat.getName();

                    String createdAtStr = (template.getCreatedAt() != null)
                            ? template.getCreatedAt().format(FORMATTER)
                            : null;
                    String updatedAtStr = (template.getUpdatedAt() != null)
                            ? template.getUpdatedAt().format(FORMATTER)
                            : null;

                    return new TicketTemplateListResponse(
                            template.getId(),
                            template.getTemplateTitle(),
                            template.getTitle(),
                            typeId,
                            typeName,
                            firstCatId,
                            firstCatName,
                            secondCatId,
                            secondCatName,
                            createdAtStr,
                            updatedAtStr
                    );
                })
                .collect(Collectors.toList());
    }
}
