package com.tracker.checkin.controller;

import com.tracker.auth.domain.User;
import com.tracker.checkin.dto.CheckInDtos.*;
import com.tracker.checkin.service.CheckInService;
import com.tracker.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Check-in endpoints — all require a valid JWT.
 *
 * GET  /api/v1/checkins/today                  — today's habits with check-in state
 * GET  /api/v1/checkins/today?date=YYYY-MM-DD  — any date's habits with check-in state
 * POST /api/v1/checkins/bulk                   — submit full day's check-in state
 * GET  /api/v1/checkins/history/{habitId}      — paginated history for a habit
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    // ── GET /api/v1/checkins/today ────────────────────────────────────────────
    //    Optional ?date=YYYY-MM-DD param — defaults to today if omitted

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<TodayHabitView>>> getToday(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @AuthenticationPrincipal User user
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(checkInService.getTodayView(user, targetDate)));
    }

    // ── POST /api/v1/checkins/bulk ────────────────────────────────────────────

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<TodayHabitView>>> bulkCheckIn(
            @Valid @RequestBody BulkCheckInRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(checkInService.bulkUpsert(request, user)));
    }

    // ── GET /api/v1/checkins/history/{habitId} ────────────────────────────────
    //    ?page=0&size=30  — defaults shown below

    @GetMapping("/history/{habitId}")
    public ResponseEntity<ApiResponse<Page<CheckInHistoryEntry>>> getHistory(
            @PathVariable UUID habitId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                checkInService.getHistory(habitId, user, page, size)
        ));
    }
}
