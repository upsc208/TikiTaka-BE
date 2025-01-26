package com.trillion.tikitaka.category.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 생성에 사용할 DTO
 * - 예: { "name": "DNS" }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    private String name;
}



