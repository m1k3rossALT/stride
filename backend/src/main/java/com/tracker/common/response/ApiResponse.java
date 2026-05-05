package com.tracker.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Unified response wrapper for every API endpoint.
 *
 * Success shape : { "success": true,  "data": {...},  "error": null,      "timestamp": "..." }
 * Error shape   : { "success": false, "data": null,   "error": {...},     "timestamp": "..." }
 *
 * All controllers return ResponseEntity<ApiResponse<T>> — nothing else.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp
) {

    // ── Factory methods ──────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message), Instant.now());
    }

    // ── Nested error record ──────────────────────────────────────────────────

    public record ApiError(String code, String message) {}
}
