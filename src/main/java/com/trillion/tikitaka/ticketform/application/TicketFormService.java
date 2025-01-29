package com.trillion.tikitaka.ticketform.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.InvalidCategoryLevelException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.ticketform.domain.TicketForm;
import com.trillion.tikitaka.ticketform.domain.TicketFormId;
import com.trillion.tikitaka.ticketform.exception.DuplicatedTicketFormException;
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
}
