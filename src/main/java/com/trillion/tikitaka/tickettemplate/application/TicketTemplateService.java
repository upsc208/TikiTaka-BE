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

        TicketType type = (req.getTypeId() != null) ? typeRepository.findById(req.getTypeId()).orElse(null) : null;
        Category firstCat = (req.getFirstCategoryId() != null) ? categoryRepository.findById(req.getFirstCategoryId()).orElse(null) : null;
        Category secondCat = (req.getSecondCategoryId() != null) ? categoryRepository.findById(req.getSecondCategoryId()).orElse(null) : null;

        if (firstCat != null && secondCat != null) {
            if (secondCat.getParent() == null || !secondCat.getParent().getId().equals(firstCat.getId())) {
                log.error("[티켓 템플릿 생성] 2차 카테고리가 1차 카테고리에 속하지 않음");
                throw new TicketTemplateInvalidFKException();
            }
        }

        User requester = userRepository.findById(userDetails.getId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        User manager = (req.getManagerId() != null) ?
                userRepository.findById(req.getManagerId()).orElse(null) : null;

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

        TicketType type = (req.getTypeId() != null) ? typeRepository.findById(req.getTypeId()).orElse(null) : null;
        Category firstCat = (req.getFirstCategoryId() != null) ? categoryRepository.findById(req.getFirstCategoryId()).orElse(null) : null;
        Category secondCat = (req.getSecondCategoryId() != null) ? categoryRepository.findById(req.getSecondCategoryId()).orElse(null) : null;

        if (firstCat != null && secondCat != null) {
            if (secondCat.getParent() == null || !secondCat.getParent().getId().equals(firstCat.getId())) {
                log.error("[티켓 템플릿 수정] 2차 카테고리가 1차 카테고리에 속하지 않음");
                throw new TicketTemplateInvalidFKException();
            }
        }

        User requester = userRepository.findById(userDetails.getId())
                .orElseThrow(TicketTemplateInvalidFKException::new);
        User manager = (req.getManagerId() != null) ?
                userRepository.findById(req.getManagerId()).orElse(null) : null;


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

    public TicketTemplateResponse getOneTicketTemplate(Long templateId, Long userId) {
        log.info("[티켓 템플릿 조회] ID: {}, 사용자 ID: {}", templateId, userId);
        TicketTemplate template = templateRepository.findById(templateId)
                .orElseThrow(TicketTemplateNotFoundException::new);

        if (!template.getRequester().getId().equals(userId)) {
            log.error("[티켓 템플릿 조회] 사용자가 권한 없음");
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        TicketType typeEntity = template.getType();
        Category firstCat = template.getFirstCategory();
        Category secondCat = template.getSecondCategory();
        User manager = template.getManager();

        Long typeId = (typeEntity != null && typeEntity.getDeletedAt() == null) ? typeEntity.getId() : null;
        String typeName = (typeEntity != null && typeEntity.getDeletedAt() == null) ? typeEntity.getName() : null;

        Long firstCatId = (firstCat != null && firstCat.getDeletedAt() == null) ? firstCat.getId() : null;
        String firstCatName = (firstCat != null && firstCat.getDeletedAt() == null) ? firstCat.getName() : null;

        Long secondCatId = (secondCat != null && secondCat.getDeletedAt() == null) ? secondCat.getId() : null;
        String secondCatName = (secondCat != null && secondCat.getDeletedAt() == null) ? secondCat.getName() : null;

        Long managerId = (manager != null && manager.getDeletedAt() == null) ? manager.getId() : null;
        String managerName = (manager != null && manager.getDeletedAt() == null) ? manager.getUsername() : null;

        User requester = template.getRequester();
        Long requesterId = requester.getId();
        String requesterName = requester.getUsername();

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
