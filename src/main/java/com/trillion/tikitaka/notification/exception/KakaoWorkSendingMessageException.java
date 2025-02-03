package com.trillion.tikitaka.notification.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class KakaoWorkSendingMessageException extends CustomException {
    public KakaoWorkSendingMessageException() {
        super(ErrorCode.SENDING_MESSAGE_FAILED);
    }
}
