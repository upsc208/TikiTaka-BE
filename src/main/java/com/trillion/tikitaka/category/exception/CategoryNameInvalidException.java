package com.trillion.tikitaka.category.exception;

/**
 * 카테고리 이름이 null, 공백, 혹은 25자 초과 등 잘못된 경우 발생
 */
public class CategoryNameInvalidException extends RuntimeException {
    public CategoryNameInvalidException(String message) {
        super(message);
    }
}
