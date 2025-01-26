package com.trillion.tikitaka.category.exception;

public class InvalidCategoryNameException extends RuntimeException {
    public InvalidCategoryNameException(String message) {
        super(message);
    }
}