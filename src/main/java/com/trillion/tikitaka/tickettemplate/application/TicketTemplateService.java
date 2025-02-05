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

    /**
     * 티켓 템플릿 생성
     */
    @Transactional
    public Long createTicketTemplate(TicketTemplateRequest req) {
        // 1) FK 유효성 검사
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

        // 2) 엔티티 생성 (ID가 아닌 엔티티를 주입)
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

        // 3) 저장
        return templateRepository.save(entity).getId();
    }

    /**
     * 티켓 템플릿 수정
     */
    @Transactional
    public void updateTicketTemplate(Long id, TicketTemplateRequest req) {
        // 1) 존재 확인
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        // 2) FK 유효성 + 엔티티 조회
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

        // 3) update
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

    /**
     * 티켓 템플릿 삭제
     */
    @Transactional
    public void deleteTicketTemplate(Long id) {
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        templateRepository.delete(template);
    }

    /**
     * 단일 조회
     */
    public TicketTemplateResponse getOneTicketTemplate(Long id) {
        TicketTemplate template = templateRepository.findById(id)
                .orElseThrow(TicketTemplateNotFoundException::new);

        // TicketType
        TicketType typeEntity = template.getType();
        Long typeId = typeEntity.getId();
        String typeName = typeEntity.getName(); // 예: TicketType 엔티티에 getName() 있다고 가정

        // Category
        Category firstCat = template.getFirstCategory();
        Long firstCatId = firstCat.getId();
        String firstCatName = firstCat.getName();

        Category secondCat = template.getSecondCategory();
        Long secondCatId = secondCat.getId();
        String secondCatName = secondCat.getName();

        // 요청자, 담당자
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

    /**
     * 목록 조회
     */
    public List<TicketTemplateListResponse> getAllTicketTemplates() {
        List<TicketTemplate> templates = templateRepository.findAll();

        return templates.stream()
                .map(template -> {
                    // type
                    TicketType typeEntity = template.getType();
                    Long typeId = typeEntity.getId();
                    String typeName = typeEntity.getName();

                    // category
                    Category firstCat = template.getFirstCategory();
                    Long firstCatId = firstCat.getId();
                    String firstCatName = firstCat.getName();

                    Category secondCat = template.getSecondCategory();
                    Long secondCatId = secondCat.getId();
                    String secondCatName = secondCat.getName();

                    // createdAt / updatedAt
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
