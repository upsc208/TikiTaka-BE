package com.trillion.tikitaka.category.domain.repository;

import com.trillion.tikitaka.category.domain.model.Category;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;

import java.util.List;
import java.util.Optional;

/**
 * Domain 레벨의 Repository 인터페이스
 * 추상화된 기능만 선언.
 * Spring Data JPA 구현체는 infrastructure에 위치 (JpaCategoryRepository)
 */
public interface CategoryRepository {

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    // parent == null 이면서 특정 id를 가진 카테고리 (1차 카테고리 판별용)
    Optional<Category> findByIdAndParentIsNull(Long id);

    // 레벨(0, 1)에 따른 카테고리 조회
    List<CategoryResponse> getCategoriesByLevel(int level);

    // ID나 이름 중 하나가 일치하는 카테고리 조회 (중복 체크용)
    List<Category> findByIdOrName(Long categoryId, String name);

    // 저장
    Category save(Category category);

    // 삭제 (Soft Delete)
    void delete(Category category);
}
