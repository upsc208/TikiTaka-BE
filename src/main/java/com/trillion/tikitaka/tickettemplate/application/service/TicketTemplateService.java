package com.trillion.tikitaka.tickettemplate.application.service;

import com.trillion.tikitaka.tickettemplate.domain.model.TicketTemplate;
import com.trillion.tikitaka.tickettemplate.domain.repository.TicketTemplateRepository;
import com.trillion.tikitaka.tickettemplate.dto.request.TicketTemplateRequest;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTemplateService {

    private final TicketTemplateRepository templateRepository;
    private final TicketTypeRepository typeRepository;          // FK 검사
    private final CategoryRepository categoryRepository;  // FK 검사
    private final UserRepository userRepository;          // FK 검사

    @Transactional
    public Long createTicketTemplate(TicketTemplateRequest req) {
        validateRequiredFields(req);
        validateFK(req);

        LocalDateTime createdDateTime = parseDateTime(req.getCreatedAt());

        // 엔티티 빌드
        TicketTemplate entity = TicketTemplate.builder()
                .templateTitle(req.getTemplateTitle())
                .title(req.getTitle())
                .description(req.getDescription())
                .typeId(req.getTypeId())
                .firstCategoryId(req.getFirstCategoryId())
                .secondCategoryId(req.getSecondCategoryId())
                .requesterId(req.getRequesterId())
                .managerId(req.getManagerId())
                .createdAt(createdDateTime)
                .build();

        // 저장
        TicketTemplate saved = templateRepository.save(entity);
        return saved.getId();
    }

    /**
     * (1) managerId 제외 모든 필드는 필수 -> null/blank 확인
     */
    private void validateRequiredFields(TicketTemplateRequest req) {
        if (req.getTemplateTitle() == null || req.getTemplateTitle().isBlank()) {
            throw new IllegalArgumentException("templateTitle is required");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        if (req.getTypeId() == null) {
            throw new IllegalArgumentException("typeId is required");
        }
        if (req.getFirstCategoryId() == null) {
            throw new IllegalArgumentException("firstCategoryId is required");
        }
        if (req.getSecondCategoryId() == null) {
            throw new IllegalArgumentException("secondCategoryId is required");
        }
        if (req.getRequesterId() == null) {
            throw new IllegalArgumentException("requesterId is required");
        }
        // managerId is optional
    }

    /**
     * (2) FK 유효성 체크
     *     - typeId → typeRepository
     *     - categoryId → categoryRepository
     *     - requesterId → userRepository
     *     - managerId → optional, 있으면 userRepository 확인해도 됨(원한다면)
     */
    private void validateFK(TicketTemplateRequest req) {
        if (typeRepository.findById(req.getTypeId()).isEmpty()) {
            throw new IllegalArgumentException("Invalid typeId: " + req.getTypeId());
        }
        if (categoryRepository.findById(req.getFirstCategoryId()).isEmpty()) {
            throw new IllegalArgumentException("Invalid firstCategoryId: " + req.getFirstCategoryId());
        }
        if (categoryRepository.findById(req.getSecondCategoryId()).isEmpty()) {
            throw new IllegalArgumentException("Invalid secondCategoryId: " + req.getSecondCategoryId());
        }
        if (userRepository.findById(req.getRequesterId()).isEmpty()) {
            throw new IllegalArgumentException("Invalid requesterId: " + req.getRequesterId());
        }

        // managerId => optional. 필요한 경우 주석 해제
        /*
        if (req.getManagerId() != null) {
            if (userRepository.findById(req.getManagerId()).isEmpty()) {
                throw new IllegalArgumentException("Invalid managerId: " + req.getManagerId());
            }
        }
        */
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now();
        }
        // yyyy-MM-dd -> yyyy-MM-ddT00:00:00
        return LocalDateTime.parse(dateStr + "T00:00:00");
    }
}
