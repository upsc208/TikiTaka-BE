package com.trillion.tikitaka.global.exception;

import com.trillion.tikitaka.global.response.ErrorResponse;
import com.trillion.tikitaka.category.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.trillion.tikitaka.category.exception.CategoryDuplicateException;
import com.trillion.tikitaka.category.exception.CategoryNotFoundException;
import com.trillion.tikitaka.category.exception.CategoryInvalidParentException;
import com.trillion.tikitaka.category.exception.CategoryNameInvalidException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode().getHttpStatus(),
                e.getErrorCode().getErrorCode(),
                e.getErrorCode().getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.ACCESS_DENIED.getHttpStatus(),
                ErrorCode.ACCESS_DENIED.getErrorCode(),
                ErrorCode.ACCESS_DENIED.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .findFirst()
                .orElse(ErrorCode.INVALID_REQUEST_VALUE.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_REQUEST_VALUE.getHttpStatus(),
                ErrorCode.INVALID_REQUEST_VALUE.getErrorCode(),
                errorMessage
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_REQUEST_VALUE.getHttpStatus(),
                ErrorCode.INVALID_REQUEST_VALUE.getErrorCode(),
                ErrorCode.INVALID_REQUEST_VALUE.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.MISSING_REQUEST_PARAMETER.getHttpStatus(),
                ErrorCode.MISSING_REQUEST_PARAMETER.getErrorCode(),
                ErrorCode.MISSING_REQUEST_PARAMETER.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_ARGUMENT_TYPE.getHttpStatus(),
                ErrorCode.INVALID_ARGUMENT_TYPE.getErrorCode(),
                ErrorCode.INVALID_ARGUMENT_TYPE.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionSystemException(TransactionSystemException e) {
        Throwable rootCause = e.getRootCause();
        String errorMessage = (rootCause != null) ? rootCause.getMessage() : e.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
                errorMessage
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(DuplicatedCategoryException.class)
    public ResponseEntity<?> handleDuplicated(DuplicatedCategoryException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<?> handleNotFound(CategoryNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCategoryNameException.class)
    public ResponseEntity<?> handleInvalidName(InvalidCategoryNameException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(PrimaryCategoryNotFoundException.class)
    public ResponseEntity<?> handlePrimaryNotFound(PrimaryCategoryNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleOther(RuntimeException ex) {
        // 기타 예외
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + ex.getMessage());
    }


}
