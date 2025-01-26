package com.trillion.tikitaka.category.exception;

public class DuplicatedCategoryException extends RuntimeException {
    public DuplicatedCategoryException(String message) {
        super(message);
    }
}