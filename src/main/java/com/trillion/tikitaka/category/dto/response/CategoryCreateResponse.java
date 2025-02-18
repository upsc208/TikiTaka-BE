package com.trillion.tikitaka.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryCreateResponse {
    private Long id;
    private String name;
}
