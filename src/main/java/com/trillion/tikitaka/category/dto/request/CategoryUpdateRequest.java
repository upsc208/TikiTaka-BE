package com.trillion.tikitaka.category.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 이름 수정 DTO
 * - patch /categories/{categoryId}
 * - 예: { "name": "네트워크" }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    private String name;
}