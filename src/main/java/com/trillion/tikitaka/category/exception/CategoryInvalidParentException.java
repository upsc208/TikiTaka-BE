package com.trillion.tikitaka.category.exception;

/**
 * 부모 카테고리가 1차가 아닌 경우 등 잘못된 부모 설정 시 발생
 */
public class CategoryInvalidParentException extends RuntimeException {
    public CategoryInvalidParentException(String message) {
        super(message);
    }
}
