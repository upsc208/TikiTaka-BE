package com.trillion.tikitaka.category.exception;

import com.trillion.tikitaka.global.exception.CustomException;

public class DuplicatedCategoryException extends CustomException {
    public DuplicatedCategoryException() {
        super(com.trillion.tikitaka.global.exception.ErrorCode.DUPLICATED_CATEGORY_NAME);
    }
}
