package com.trillion.tikitaka.category.presentation.controller;

import com.trillion.tikitaka.category.application.service.CategoryService;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.request.CategoryUpdateRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 생성
     * 요청 Body: { "name": "DNS" }
     * 쿼리 param: /categories?parentId=3 => 3번 카테고리를 부모로 둔 2차 카테고리
     */
    @PostMapping
    public ResponseEntity<String> createCategory(
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestBody CategoryRequest request
    ) {
        categoryService.createCategory(parentId, request);
        return ResponseEntity.ok("Category Created");
    }

    /**
     * 카테고리 조회
     * ex) GET /categories?level=0 => 1차 카테고리 목록
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(value = "level", defaultValue = "0") int level
    ) {
        List<CategoryResponse> categories = categoryService.getCategories(level);
        return ResponseEntity.ok(categories);
    }

    /**
     * 카테고리 이름 수정
     * PATCH /categories/{categoryId}
     * Body: { "name": "네트워크" }
     */
    @PatchMapping("/{categoryId}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryUpdateRequest request
    ) {
        categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok("Category Updated");
    }

    /**
     * 카테고리 삭제 (Soft Delete)
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category Deleted");
    }
}
