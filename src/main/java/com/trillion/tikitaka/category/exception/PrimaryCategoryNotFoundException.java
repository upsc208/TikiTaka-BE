package com.trillion.tikitaka.category.exception;

public class PrimaryCategoryNotFoundException extends RuntimeException {
    public PrimaryCategoryNotFoundException(String message) {
        super(message);
    }
}