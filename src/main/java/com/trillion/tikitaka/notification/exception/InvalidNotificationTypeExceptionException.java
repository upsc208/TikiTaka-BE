package com.trillion.tikitaka.notification.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidNotificationTypeExceptionException extends CustomException {
    public InvalidNotificationTypeExceptionException() {
        super(ErrorCode.INVALID_NOTIFICATION_TYPE);
    }
}
