package com.tracker.habit.controller;

import com.tracker.auth.domain.User;
import com.tracker.common.response.ApiResponse;
import com.tracker.habit.dto.HabitDtos.*;
import com.tracker.habit.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Habit endpoints — all require a valid JWT.
 *
 * GET    /api/v1/habits              — list active habits for the current user
 * POST   /api/v1/habits              — create a new habit
 * PUT    /api/v1/habits/{id}         — edit name/description
 * PATCH  /api/v1/habits/{id}/archive — archive a habit
 * PATCH  /api/v1/habits/{id}/restore — restore an archived habit
 * DELETE /api/v1/habits/{id}         — permanently delete a habit
 * GET    /api/v1/habits/archived     — list archived habits
 *
 * @AuthenticationPrincipal User resolves the currently logged-in user
 * directly from the JWT security context — no manual token parsing needed.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    // ── GET /api/v1/habits ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<HabitResponse>>> getActive(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(habitService.getActive(user)));
    }

    // ── GET /api/v1/habits/archived ──────────────────────────────────────────

    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<List<HabitResponse>>> getArchived(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(habitService.getArchived(user)));
    }

    // ── POST /api/v1/habits ──────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<HabitResponse>> create(
            @Valid @RequestBody CreateHabitRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(habitService.create(request, user)));
    }

    // ── PUT /api/v1/habits/{id} ──────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HabitResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHabitRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(habitService.update(id, request, user)));
    }

    // ── PATCH /api/v1/habits/{id}/archive ────────────────────────────────────

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<HabitResponse>> archive(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(habitService.archive(id, user)));
    }

    // ── PATCH /api/v1/habits/{id}/restore ────────────────────────────────────

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<HabitResponse>> restore(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(habitService.restore(id, user)));
    }

    // ── DELETE /api/v1/habits/{id} ───────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        habitService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
