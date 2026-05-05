package com.tracker.habit.domain;

/**
 * Lifecycle states for a habit.
 *
 * ACTIVE   — visible on the daily dashboard, can be checked in
 * ARCHIVED — hidden from the dashboard, fully restorable, history intact
 * DELETED  — permanently removed, history wiped, cannot be undone
 *
 * Valid transitions:
 *   ACTIVE   → ARCHIVED  (user archives)
 *   ACTIVE   → DELETED   (user permanently deletes)
 *   ARCHIVED → ACTIVE    (user restores)
 *   ARCHIVED → DELETED   (user permanently deletes from archive)
 *   DELETED  → (nothing) — terminal state
 */
public enum HabitStatus {
    ACTIVE,
    ARCHIVED,
    DELETED
}
