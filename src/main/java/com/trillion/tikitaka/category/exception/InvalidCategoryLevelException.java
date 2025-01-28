package com.trillion.tikitaka.category.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidCategoryLevelException extends CustomException {
    public InvalidCategoryLevelException() {
        super(ErrorCode.INVALID_CATEGORY_LEVEL);
    }
}
