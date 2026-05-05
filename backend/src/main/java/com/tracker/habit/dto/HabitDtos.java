package com.tracker.habit.dto;

import com.tracker.habit.domain.HabitStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * DTOs for the habit module.
 *
 * Inbound  — CreateHabitRequest, UpdateHabitRequest
 * Outbound — HabitResponse
 */
public class HabitDtos {

    // ── Inbound ──────────────────────────────────────────────────────────────

    public record CreateHabitRequest(

            @NotBlank(message = "Habit name is required")
            @Size(min = 1, max = 100, message = "Habit name must be between 1 and 100 characters")
            String name,

            @Size(max = 255, message = "Description cannot exceed 255 characters")
            String description

    ) {}

    public record UpdateHabitRequest(

            @NotBlank(message = "Habit name is required")
            @Size(min = 1, max = 100, message = "Habit name must be between 1 and 100 characters")
            String name,

            @Size(max = 255, message = "Description cannot exceed 255 characters")
            String description

    ) {}

    // ── Outbound ─────────────────────────────────────────────────────────────

    public record HabitResponse(
            UUID        id,
            String      name,
            String      description,
            HabitStatus status,
            Instant     createdAt,
            Instant     updatedAt
    ) {}
}
