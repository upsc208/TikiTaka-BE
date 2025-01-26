package com.trillion.tikitaka.category.exception;

/**
 * 부모 카테고리 등이 존재하지 않을 때 발생
 */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
