package com.trillion.tikitaka.notification.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class InvalidNotificationTypeException extends CustomException {
    public InvalidNotificationTypeException() {
        super(ErrorCode.INVALID_NOTIFICATION_TYPE);
    }
}
