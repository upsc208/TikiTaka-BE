package com.trillion.tikitaka.tickettemplate.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    public Long createTicketTemplate(TicketTemplateRequest req, CustomUserDetails userDetails) {
        log.info("[티켓 템플릿 생성] 템플릿 제목: {}, 제목: {}", req.getTemplateTitle(), req.getTitle());
        TicketType type = typeRepository.findById(req.getTypeId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category firstCat = categoryRepository.findById(req.getFirstCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category secondCat = categoryRepository.findById(req.getSecondCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);

        User requester = userRepository.findById(userDetails.getId())
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
    public Long updateTicketTemplate(Long id, TicketTemplateRequest req, CustomUserDetails userDetails) {
        log.info("[티켓 템플릿 수정] ID: {}, 템플릿 제목: {}, 제목: {}", id, req.getTemplateTitle(), req.getTitle());
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        if (!template.getRequester().getId().equals(userDetails.getId())) {
            log.error("[티켓 템플릿 수정] 권한 없음");
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        TicketType type = typeRepository.findById(req.getTypeId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category firstCat = categoryRepository.findById(req.getFirstCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        Category secondCat = categoryRepository.findById(req.getSecondCategoryId())
                .orElseThrow(TicketTemplateInvalidFKException::new);

        User requester = userRepository.findById(userDetails.getId())
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

        return template.getId();
    }

    @Transactional
    public void deleteTicketTemplate(Long id, CustomUserDetails userDetails) {
        log.info("[티켓 템플릿 삭제] ID: {}", id);
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        if (!template.getRequester().getId().equals(userDetails.getId())) {
            log.error("[티켓 템플릿 삭제] 권한 없음");
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        templateRepository.delete(template);
    }

    public TicketTemplateResponse getOneTicketTemplate(Long id) {
        log.info("[티켓 템플릿 조회] ID: {}", id);
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
                template.getId(),
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

    public List<TicketTemplateListResponse> getMyTemplates(CustomUserDetails userDetails) {
        log.info("[티켓 템플릿 전체 조회]");
        return templateRepository.getAllTemplates(userDetails.getId());
    }
}
