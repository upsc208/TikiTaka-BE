package com.trillion.tikitaka.category.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.DuplicatedCategoryException;
import com.trillion.tikitaka.category.exception.PrimaryCategoryNotFoundException;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void createCategory(Long parentId, CategoryRequest categoryRequest) {
        categoryRepository.findByName(categoryRequest.getName())
                .ifPresent(c -> {
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

    public List<CategoryResponse> getCategories() {
        // 예시: 1차 카테고리만 가져온다 (level=0)
        return categoryRepository.getCategoriesByLevel(0);
        // 필요 시 level 파라미터를 받아서 처리 가능
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryRequest request) {
        List<Category> categories = categoryRepository.findByIdOrName(categoryId, request.getName());

        boolean idExists = categories.stream().anyMatch(category -> category.getId().equals(categoryId));
        boolean nameExists = categories.stream().anyMatch(category -> category.getName().equals(request.getName()));

        if (!idExists) throw new CategoryNotFoundException();
        if (nameExists) throw new DuplicatedCategoryException();

        Category category = categories.stream()
                .filter(cat -> cat.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        category.updateName(request.getName());
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        categoryRepository.deleteAll(category.getChildren());
    }
}
