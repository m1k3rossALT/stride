package com.tracker.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base runtime exception for all deliberate application errors.
 *
 * Usage:
 *   throw new AppException(ErrorCode.HABIT_NOT_FOUND);
 *   throw new AppException(ErrorCode.HABIT_NOT_FOUND, "Habit ID " + id + " does not exist");
 *
 * The GlobalExceptionHandler catches this and maps it to the correct HTTP status + response envelope.
 */
@Getter
public class AppException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode  = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    // Use this overload when you want to give more detail in the message
    public AppException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode  = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
