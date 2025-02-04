package com.trillion.tikitaka.notification.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class KakaoWorkOpeningConversationException extends CustomException {
    public KakaoWorkOpeningConversationException() {
        super(ErrorCode.FETCHING_CONVERSATION_ID_FAILED);
    }
}
