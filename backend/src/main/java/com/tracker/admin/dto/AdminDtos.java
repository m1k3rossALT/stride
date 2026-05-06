package com.tracker.admin.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTOs for the admin module.
 *
 * Privacy rules enforced at this layer:
 *   - No password_hash in any response
 *   - No email address in any response
 *   - No check-in content (what the habit is, whether completed) — counts only
 *
 * Admin sees: who exists, when they joined, how active they are.
 * Admin never sees: credentials, personal data, habit names, check-in details.
 */
public class AdminDtos {

    /**
     * One row in the user list — identity and join date only.
     */
    public record UserSummary(
            UUID    id,
            String  username,
            String  role,
            Instant createdAt
    ) {}

    /**
     * Activity breakdown for a single user.
     * Numbers only — no content, no habit names, no check-in values.
     */
    public record UserActivity(
            UUID    userId,
            String  username,
            long    activeHabits,
            long    archivedHabits,
            long    totalCheckIns
    ) {}

    /**
     * System-wide stats — shown at the top of the admin panel.
     */
    public record SystemStats(
            long totalUsers,
            long totalCheckIns
    ) {}
}
