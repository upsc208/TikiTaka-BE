package com.trillion.tikitaka.category.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.DuplicatedCategoryException;
import com.trillion.tikitaka.category.exception.PrimaryCategoryNotFoundException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void createCategory(Long parentId, CategoryRequest categoryRequest) {
        log.info("[카테고리 생성 요청]");
        categoryRepository.findByName(categoryRequest.getName())
                .ifPresent(c -> {
                    log.error("[카테고리 생성 실패] 중복된 카테고리명: {}", categoryRequest.getName());
                    throw new DuplicatedCategoryException();
                });

        Category parentCategory = null;
        if (parentId != null) {
            parentCategory = categoryRepository.findByIdAndParentIsNull(parentId)
                    .orElseThrow(PrimaryCategoryNotFoundException::new);
        }

        Category category = new Category(categoryRequest.getName(), parentCategory);
        categoryRepository.save(category);
    }

    public List<CategoryResponse> getCategories(Long parentId) {
        log.info("[카테고리 조회 요청]");
        if (parentId != null) {
            categoryRepository.findById(parentId)
                    .orElseThrow(CategoryNotFoundException::new);
        }

        return categoryRepository.getCategories(parentId);
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryRequest request) {
        log.info("[카테고리 수정 요청] 카테고리 ID: {}", categoryId);
        List<Category> categories = categoryRepository.findByIdOrName(categoryId, request.getName());

        boolean idExists = categories.stream().anyMatch(category -> category.getId().equals(categoryId));
        boolean nameExists = categories.stream().anyMatch(category -> category.getName().equals(request.getName()));

        if (!idExists) {
            log.error("[카테고리 수정 실패] 존재하지 않는 카테고리: {}", categoryId);
            throw new CategoryNotFoundException();
        }
        if (nameExists) {
            log.error("[카테고리 수정 실패] 중복된 카테고리명: {}", request.getName());
            throw new DuplicatedCategoryException();
        }

        Category category = categories.stream()
                .filter(cat -> cat.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        category.updateName(request.getName());
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("[카테고리 삭제 요청] 카테고리 ID: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        categoryRepository.deleteAll(category.getChildren());
        categoryRepository.delete(category);
    }
}
