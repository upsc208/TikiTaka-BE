package com.trillion.tikitaka.category.infrastructure;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Optional<Category> findByIdAndParentIsNull(Long id);

    List<CategoryResponse> getCategoriesByLevel(int level);

    List<Category> findByIdOrName(Long categoryId, String name);

    Category save(Category category);

    void delete(Category category);
}
