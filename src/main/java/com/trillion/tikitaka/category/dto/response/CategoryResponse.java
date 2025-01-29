package com.trillion.tikitaka.category.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;

    @QueryProjection
    public CategoryResponse(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}
