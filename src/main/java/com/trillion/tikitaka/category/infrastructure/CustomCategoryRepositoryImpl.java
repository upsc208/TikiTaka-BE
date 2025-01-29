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
    public List<CategoryResponse> getCategories(Long parentId) {

        return queryFactory
                .select(new QCategoryResponse(
                        category.id,
                        category.name,
                        category.parent.id.as("parentId")
                ))
                .from(category)
                .where(
                        parentIdEq(parentId)
                )
                .fetch();
    }

    private BooleanExpression parentIdEq(Long parentId) {
        if (parentId == null) {
            return category.parent.isNull();
        } else {
            return category.parent.id.eq(parentId);
        }
    }
}
