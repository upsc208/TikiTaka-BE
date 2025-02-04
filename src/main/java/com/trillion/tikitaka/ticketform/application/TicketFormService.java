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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketFormService {

    private final TicketFormRepository ticketFormRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createTicketForm(Long firstCategoryId, Long secondCategoryId, String description) {
        Category firstCategory = categoryRepository.findById(firstCategoryId)
                .orElseThrow(CategoryNotFoundException::new);
        Category secondCategory = categoryRepository.findById(secondCategoryId)
                .orElseThrow(CategoryNotFoundException::new);

        if (!secondCategory.isChildOf(firstCategory)) {
            throw new InvalidCategoryLevelException();
        }

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        if (ticketFormRepository.existsById(ticketFormId)) {
            throw new DuplicatedTicketFormException();
        }

        TicketForm ticketForm = TicketForm.builder()
                .firstCategory(firstCategory)
                .secondCategory(secondCategory)
                .description(description)
                .build();
        ticketFormRepository.save(ticketForm);
    }

    public TicketFormResponse getTicketForm(Long firstCategoryId, Long secondCategoryId) {
        validateCategoryRelationship(firstCategoryId, secondCategoryId);

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        TicketForm ticketForm = ticketFormRepository.findById(ticketFormId).orElse(null);

        String description = "";
        if (ticketForm != null) {
            description = ticketForm.getDescription();
        }

        return new TicketFormResponse(description);
    }

    @Transactional
    public void updateTicketForm(Long firstCategoryId, Long secondCategoryId, String newDescription) {
        validateCategoryRelationship(firstCategoryId, secondCategoryId);

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        TicketForm ticketForm = ticketFormRepository.findById(ticketFormId)
                .orElseThrow(TicketFormNotFoundException::new);

        ticketForm.updateDescription(newDescription);
    }

    @Transactional
    public void deleteTicketForm(Long firstCategoryId, Long secondCategoryId) {
        validateCategoryRelationship(firstCategoryId, secondCategoryId);

        TicketFormId ticketFormId = new TicketFormId(firstCategoryId, secondCategoryId);
        TicketForm ticketForm = ticketFormRepository.findById(ticketFormId)
                .orElseThrow(TicketFormNotFoundException::new);

        ticketFormRepository.delete(ticketForm);
    }

    private void validateCategoryRelationship(Long firstCategoryId, Long secondCategoryId) {
        Category secondCategory = categoryRepository.findById(secondCategoryId)
                .orElseThrow(CategoryNotFoundException::new);

        if (secondCategory.getParent() == null || !secondCategory.getParent().getId().equals(firstCategoryId)) {
            throw new InvalidCategoryLevelException();
        }
    }
}