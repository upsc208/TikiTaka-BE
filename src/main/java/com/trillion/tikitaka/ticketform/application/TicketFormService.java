package com.trillion.tikitaka.ticketform.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import com.trillion.tikitaka.ticketform.dto.response.TicketFormResponse;
import com.trillion.tikitaka.ticketform.exception.DuplicatedTicketFormException;
import com.trillion.tikitaka.ticketform.exception.TicketFormNotFoundException;
import com.trillion.tikitaka.ticketform.infrastructure.TicketFormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketFormService {

    private final TicketFormRepository ticketFormRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createTicketForm(Long firstCategoryId, Long secondCategoryId, String mustDescription, String description) {
        log.info("[티켓 폼 생성] 1차 카테고리 ID: {}, 2차 카테고리 ID: {}", firstCategoryId, secondCategoryId);
        Category firstCategory = categoryRepository.findById(firstCategoryId)
                .orElseThrow(CategoryNotFoundException::new);
        Category secondCategory = categoryRepository.findById(secondCategoryId)
                .orElseThrow(CategoryNotFoundException::new);

        if (!secondCategory.isChildOf(firstCategory)) {
            log.error("[티켓 폼 생성] 2차 카테고리가 1차 카테고리의 하위 카테고리가 아님");
            throw new InvalidCategoryLevelException();
        }

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        if (ticketFormRepository.existsById(ticketFormId)) {
            log.error("[티켓 폼 생성] 중복된 티켓 폼");
            throw new DuplicatedTicketFormException();
        }

        TicketForm ticketForm = TicketForm.builder()
                .firstCategory(firstCategory)
                .secondCategory(secondCategory)
                .mustDescription(mustDescription)
                .description(description)
                .build();
        ticketFormRepository.save(ticketForm);
    }

    public TicketFormResponse getTicketForm(Long firstCategoryId, Long secondCategoryId) {
        log.info("[티켓 폼 조회] 1차 카테고리 ID: {}, 2차 카테고리 ID: {}", firstCategoryId, secondCategoryId);
        validateCategoryRelationship(firstCategoryId, secondCategoryId);

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        TicketForm ticketForm = ticketFormRepository.findById(ticketFormId).orElse(null);

        String mustDescription = "";
        String description = "";
        if (ticketForm != null) {
            mustDescription = ticketForm.getMustDescription();
            description = ticketForm.getDescription();
        }

        return new TicketFormResponse(mustDescription, description);
    }

    @Transactional
    public void updateTicketForm(Long firstCategoryId, Long secondCategoryId, String newMustDescription, String newDescription) {
        log.info("[티켓 폼 수정] 1차 카테고리 ID: {}, 2차 카테고리 ID: {}", firstCategoryId, secondCategoryId);
        validateCategoryRelationship(firstCategoryId, secondCategoryId);

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        TicketForm ticketForm = ticketFormRepository.findById(ticketFormId)
                .orElseThrow(TicketFormNotFoundException::new);

        ticketForm.update(newMustDescription, newDescription);
    }

    @Transactional
    public void deleteTicketForm(Long firstCategoryId, Long secondCategoryId) {
        log.info("[티켓 폼 삭제] 1차 카테고리 ID: {}, 2차 카테고리 ID: {}", firstCategoryId, secondCategoryId);
        validateCategoryRelationship(firstCategoryId, secondCategoryId);

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        TicketForm ticketForm = ticketFormRepository.findById(ticketFormId)
                .orElseThrow(TicketFormNotFoundException::new);

        ticketFormRepository.delete(ticketForm);
    }

    private void validateCategoryRelationship(Long firstCategoryId, Long secondCategoryId) {
        log.info("[카테고리 관계 검증] 1차 카테고리 ID: {}, 2차 카테고리 ID: {}", firstCategoryId, secondCategoryId);
        Category secondCategory = categoryRepository.findById(secondCategoryId)
                .orElseThrow(CategoryNotFoundException::new);

        if (secondCategory.getParent() == null || !secondCategory.getParent().getId().equals(firstCategoryId)) {
            log.error("[카테고리 관계 검증] 2차 카테고리가 1차 카테고리의 하위 카테고리가 아님");
            throw new InvalidCategoryLevelException();
        }
    }
}
