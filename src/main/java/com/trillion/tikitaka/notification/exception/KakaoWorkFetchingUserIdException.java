package com.trillion.tikitaka.notification.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class KakaoWorkFetchingUserIdException extends CustomException {
    public KakaoWorkFetchingUserIdException() {
        super(ErrorCode.FETCHING_USER_ID_FAILED);
    }
}
