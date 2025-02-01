package com.trillion.tikitaka.subtask.exception;

import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;

public class SubtaskNotFoundExeption extends CustomException {

    public SubtaskNotFoundExeption(){super(ErrorCode.SUBTASK_NOT_FOUND);}
}
