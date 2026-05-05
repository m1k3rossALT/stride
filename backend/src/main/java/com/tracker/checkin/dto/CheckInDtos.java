package com.tracker.checkin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for the check-in module.
 *
 * Inbound:
 *   BulkCheckInRequest  — submits checked state for all habits in one call
 *
 * Outbound:
 *   TodayHabitView      — a habit with its check-in status for today's dashboard
 *   CheckInHistoryEntry — a single date entry in a habit's history
 */
public class CheckInDtos {

    // ── Inbound ──────────────────────────────────────────────────────────────

    /**
     * Submits the full day's check-in state in one request.
     * Each entry is a habit ID + checked boolean.
     * Habits not included in the list are left unchanged.
     */
    public record BulkCheckInRequest(

            @NotNull(message = "Check-in date is required")
            LocalDate date,

            @NotNull(message = "Items list is required")
            @Valid
            List<CheckInItem> items

    ) {
        public record CheckInItem(

                @NotNull(message = "Habit ID is required")
                UUID habitId,

                boolean checked

        ) {}
    }

    // ── Outbound ─────────────────────────────────────────────────────────────

    /**
     * One row on the daily dashboard — the habit details plus today's check-in state.
     * isChecked is false if no check-in record exists yet for today.
     */
    public record TodayHabitView(
            UUID      habitId,
            String    habitName,
            String    habitDescription,
            LocalDate date,
            boolean   isChecked
    ) {}

    /**
     * One entry in a habit's history — date and whether it was completed.
     */
    public record CheckInHistoryEntry(
            LocalDate date,
            boolean   isChecked
    ) {}
}
