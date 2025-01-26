package com.trillion.tikitaka.category.infrastructure.persistence;

import com.trillion.tikitaka.category.domain.model.Category;
import com.trillion.tikitaka.category.domain.repository.CategoryRepository;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA 구현체.
 * Domain 레벨 CategoryRepository를 확장(implements)하고,
 * JpaRepository<Category, Long>을 통해 실제 DB 접근을 수행함.
 */
public interface JpaCategoryRepository
        extends JpaRepository<Category, Long>, CategoryRepository {

    /**
     * 이름으로 카테고리 찾기.
     * (논리삭제는 @SQLRestriction으로 필터됨)
     */
    @Override
    Optional<Category> findByName(String name);

    /**
     * parent == null && id == 특정 값
     * => 1차 카테고리 판별용
     */
    @Override
    Optional<Category> findByIdAndParentIsNull(Long id);

    /**
     * 레벨별 카테고리 조회
     * level=0 => parent IS NULL (1차)
     * level=1 => parent IS NOT NULL (2차)
     */
    @Override
    @Query("""
        SELECT new com.trillion.tikitaka.category.dto.response.CategoryResponse(
            c.id,
            c.name,
            CASE WHEN c.parent IS NULL THEN null ELSE c.parent.id END
        )
        FROM Category c
        WHERE
            (:level = 0 AND c.parent IS NULL)
            OR
            (:level = 1 AND c.parent IS NOT NULL)
    """)
    List<CategoryResponse> getCategoriesByLevel(int level);

    /**
     * 특정 ID 또는 이름을 가진 카테고리를 한꺼번에 조회
     * => 업데이트 시 중복체크 등
     */
    @Override
    @Query("""
        SELECT c
        FROM Category c
        WHERE c.id = :categoryId
           OR c.name = :name
    """)
    List<Category> findByIdOrName(Long categoryId, String name);

    //
    // NOTE:
    // Spring Data JPA가 "findById, save, delete" 등
    // JpaRepository의 표준 메서드를 기본 제공하므로
    // 여기서 굳이 재정의할 필요가 없습니다.
    //
    // (ex: super.findById(...) 식으로 default 재정의 시 컴파일 오류 발생)
    //
}
