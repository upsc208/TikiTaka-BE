package com.trillion.tikitaka.category.application.service;

import com.trillion.tikitaka.category.domain.model.Category;
import com.trillion.tikitaka.category.domain.repository.CategoryRepository;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.request.CategoryUpdateRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.DuplicatedCategoryException;
import com.trillion.tikitaka.category.exception.InvalidCategoryNameException;
import com.trillion.tikitaka.category.exception.PrimaryCategoryNotFoundException;
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
    public void createCategory(Long parentId, CategoryRequest request) {
        validateName(request.getName());

        // 중복 이름 체크
        categoryRepository.findByName(request.getName())
                .ifPresent(c -> {
                    throw new DuplicatedCategoryException("이미 존재하는 카테고리 이름입니다.");
                });

        // parentId가 null이 아니면 2차 카테고리
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findByIdAndParentIsNull(parentId)
                    .orElseThrow(() -> new PrimaryCategoryNotFoundException("유효한 1차 카테고리를 찾을 수 없습니다."));
        }

        Category category = Category.builder()
                .name(request.getName())
                .parent(parent)
                .build();

        categoryRepository.save(category);
    }

    public List<CategoryResponse> getCategories(int level) {
        return categoryRepository.getCategoriesByLevel(level);
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryUpdateRequest request) {
        validateName(request.getName());

        List<Category> found = categoryRepository.findByIdOrName(categoryId, request.getName());

        boolean idExists = found.stream()
                .anyMatch(c -> c.getId().equals(categoryId));
        if (!idExists) {
            throw new CategoryNotFoundException("카테고리를 찾을 수 없습니다. id=" + categoryId);
        }

        boolean nameExists = found.stream()
                .anyMatch(c -> c.getName().equals(request.getName()));
        if (nameExists) {
            throw new DuplicatedCategoryException("이미 존재하는 카테고리 이름입니다: " + request.getName());
        }

        Category target = found.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. id=" + categoryId));

        target.updateName(request.getName());
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. id=" + categoryId));

        // @SQLDelete → deleted_at = now()
        categoryRepository.delete(category);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 25) {
            throw new InvalidCategoryNameException("카테고리 이름은 25자 이하의 유효한 문자열이어야 합니다.");
        }
    }
}
