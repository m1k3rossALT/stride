package com.tracker.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Single source of truth for all application error codes.
 *
 * Adding a new error: add an enum constant here.
 * Throwing an error: throw new AppException(ErrorCode.X)
 * No magic strings anywhere else in the codebase.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Auth ─────────────────────────────────────────────────────────────────
    USER_NOT_FOUND          ("USER_NOT_FOUND",           "User not found",                                   HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS     ("USER_ALREADY_EXISTS",      "Username or email is already in use",              HttpStatus.CONFLICT),
    INVALID_CREDENTIALS     ("INVALID_CREDENTIALS",      "Invalid username or password",                     HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN           ("INVALID_TOKEN",             "Token is invalid or has expired",                  HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED           ("ACCESS_DENIED",             "You do not have permission to perform this action",HttpStatus.FORBIDDEN),

    // ── Habits ───────────────────────────────────────────────────────────────
    HABIT_NOT_FOUND         ("HABIT_NOT_FOUND",          "Habit not found",                                  HttpStatus.NOT_FOUND),
    HABIT_ALREADY_DELETED   ("HABIT_ALREADY_DELETED",    "This habit has already been permanently deleted",  HttpStatus.GONE),
    HABIT_NOT_ARCHIVED      ("HABIT_NOT_ARCHIVED",       "Only archived habits can be restored",             HttpStatus.BAD_REQUEST),

    // ── Check-ins ────────────────────────────────────────────────────────────
    CHECKIN_CONFLICT        ("CHECKIN_CONFLICT",         "A check-in for this habit and date already exists",HttpStatus.CONFLICT),

    // ── Generic ──────────────────────────────────────────────────────────────
    VALIDATION_ERROR        ("VALIDATION_ERROR",         "Request validation failed",                        HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR          ("INTERNAL_ERROR",           "An unexpected error occurred",                     HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
