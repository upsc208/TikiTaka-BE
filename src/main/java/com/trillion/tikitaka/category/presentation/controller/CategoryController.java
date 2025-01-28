package com.trillion.tikitaka.category.presentation.controller;

import com.trillion.tikitaka.category.application.service.CategoryService;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 컨트롤러 예시: Admin 권한만 접근할 수 있다고 가정
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCategory(@RequestBody CategoryRequest request) {
        categoryService.create(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> list = categoryService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryRequest request
    ) {
        categoryService.update(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
