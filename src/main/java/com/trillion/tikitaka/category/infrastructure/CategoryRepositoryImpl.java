package com.trillion.tikitaka.category.infrastructure;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA
 * - Domain의 CategoryRepository + JpaRepository<Category, Long>
 */
public interface CategoryRepositoryImpl
        extends JpaRepository<Category, Long>, CategoryRepository {

    @Override
    Optional<Category> findById(Long id);

    @Override
    Optional<Category> findByName(String name);

    @Override
    Optional<Category> findByIdAndParentIsNull(Long id);

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

    @Override
    @Query("""
        SELECT c
        FROM Category c
        WHERE c.id = :categoryId
           OR c.name = :name
    """)
    List<Category> findByIdOrName(Long categoryId, String name);

    // JpaRepository의 save, delete -> 그대로 사용
}
