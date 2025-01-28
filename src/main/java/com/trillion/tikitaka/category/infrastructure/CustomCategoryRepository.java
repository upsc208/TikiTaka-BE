package com.trillion.tikitaka.category.infrastructure;

import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomCategoryRepository {
    List<CategoryResponse> getCategories(int level);
}
