package com.trillion.tikitaka.category.infrastructure;

import com.trillion.tikitaka.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, CustomCategoryRepository {
    Optional<Category> findByName(String name);
    Optional<Category> findByIdAndParentIsNull(Long id);
    List<Category> findByIdOrName(Long id, String name);
}
