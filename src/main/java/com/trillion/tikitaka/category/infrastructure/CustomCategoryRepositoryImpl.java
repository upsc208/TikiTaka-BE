package com.trillion.tikitaka.category.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.category.dto.response.QCategoryResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.trillion.tikitaka.category.domain.QCategory.category;

@RequiredArgsConstructor
public class CustomCategoryRepositoryImpl implements CustomCategoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CategoryResponse> getCategories(int level) {

        return queryFactory
                .select(new QCategoryResponse(
                        category.id,
                        category.name,
                        category.parent.id.as("parentId")
                ))
                .from(category)
                .where(
                        levelEq(level)
                )
                .fetch();
    }

    private BooleanExpression levelEq(int level) {
        if (level == 1) {
            return category.parent.isNull();
        } else if (level == 2) {
            return category.parent.isNotNull();
        }
        return null;
    }
}
