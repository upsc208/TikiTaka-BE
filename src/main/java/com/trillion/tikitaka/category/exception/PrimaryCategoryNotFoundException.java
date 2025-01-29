package com.trillion.tikitaka.category.exception;


import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class PrimaryCategoryNotFoundException extends CustomException {
    public PrimaryCategoryNotFoundException() {
        super(ErrorCode.PRIMARY_CATEGORY_NOT_FOUND);
    }
}
