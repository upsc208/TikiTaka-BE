package com.trillion.tikitaka.category.application.service;

import com.trillion.tikitaka.category.domain.model.Category;
import com.trillion.tikitaka.category.domain.repository.CategoryRepository;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
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
    public void create(CategoryRequest request) {
        // 이름 검증
        validateName(request.getName());

        // 중복 체크
        categoryRepository.findByName(request.getName()).ifPresent(c -> {
            throw new CustomException(ErrorCode.DUPLICATED_CATEGORY_NAME);
        });

        // 2차 카테고리면 parentId가 1차 카테고리인지 체크
        Category parent = null;
        if (request.getParentCategoryId() != null) {
            parent = categoryRepository.findByIdAndParentIsNull(request.getParentCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRIMARY_CATEGORY_NOT_FOUND));
        }

        // 생성
        Category category = Category.builder()
                .name(request.getName())
                .parent(parent)
                .build();
        categoryRepository.save(category);
    }

    public List<CategoryResponse> getCategories() {
        // 예시: 1차 카테고리만 가져온다 (level=0)
        return categoryRepository.getCategoriesByLevel(0);
        // 필요 시 level 파라미터를 받아서 처리 가능
    }

    @Transactional
    public void update(Long categoryId, CategoryRequest request) {
        validateName(request.getName());

        // id나 name이 같은 카테고리를 한꺼번에 조회 (중복 체크)
        List<Category> found = categoryRepository.findByIdOrName(categoryId, request.getName());

        boolean idExists = found.stream().anyMatch(c -> c.getId().equals(categoryId));
        if (!idExists) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        boolean nameExists = found.stream().anyMatch(c -> c.getName().equals(request.getName()));
        if (nameExists) {
            throw new CustomException(ErrorCode.DUPLICATED_CATEGORY_NAME);
        }

        Category target = found.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 수정
        target.updateName(request.getName());
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // Soft delete (@SQLDelete)
        categoryRepository.delete(category);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 25) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY_NAME);
        }
    }
}
