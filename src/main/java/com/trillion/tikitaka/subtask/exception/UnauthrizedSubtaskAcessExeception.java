package com.trillion.tikitaka.subtask.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class UnauthrizedSubtaskAcessExeception extends CustomException {
    public UnauthrizedSubtaskAcessExeception(){super(ErrorCode.UNAUTHORIZED_SUBTASK_ACCESS);}


}
