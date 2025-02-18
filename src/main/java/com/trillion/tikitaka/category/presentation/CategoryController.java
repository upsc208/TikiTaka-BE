package com.trillion.tikitaka.category.presentation;

import com.trillion.tikitaka.category.application.CategoryService;
import com.trillion.tikitaka.category.dto.request.CategoryRequest;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createCategory(
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestBody @Valid CategoryRequest categoryRequest) {
        Long createdId = categoryService.createCategory(parentId, categoryRequest);
        return new ResponseEntity<>(new ApiResponse<>("요청이 성공적으로 처리되었습니다", createdId), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<List<CategoryResponse>> getCategories(@RequestParam(value = "parentId", required = false) Long parentId) {
        List<CategoryResponse> categories = categoryService.getCategories(parentId);
        return new ApiResponse<>(categories);
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> updateCategory(@PathVariable("categoryId") Long categoryId,
                                            @RequestBody @Valid CategoryRequest categoryRequest) {
        categoryService.updateCategory(categoryId, categoryRequest);
        return new ApiResponse<>(null);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return new ApiResponse<>(null);
    }
}