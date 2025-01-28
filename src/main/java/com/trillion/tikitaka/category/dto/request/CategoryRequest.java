package com.trillion.tikitaka.category.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 생성/수정 시 Body로 사용하는 DTO 예시
 *  ex) {"name": "DNS", "parentCategoryId": 1}
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    private String name;
    private Long parentCategoryId;
}
