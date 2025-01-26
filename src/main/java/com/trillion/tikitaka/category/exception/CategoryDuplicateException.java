package com.trillion.tikitaka.category.exception;

/**
 * 카테고리 이름이 중복된 경우 발생
 */
public class CategoryDuplicateException extends RuntimeException {
    public CategoryDuplicateException(String message) {
        super(message);
    }
}
