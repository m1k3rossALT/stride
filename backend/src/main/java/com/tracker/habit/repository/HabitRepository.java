package com.tracker.habit.repository;

import com.tracker.habit.domain.Habit;
import com.tracker.habit.domain.HabitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for the habits table.
 *
 * All queries are scoped to a specific user_id.
 * This is a security boundary — no query here can return another user's habits.
 * The service layer passes the authenticated user's ID into every call.
 */
@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID> {

    // All habits for a user with a specific status (e.g. ACTIVE for dashboard)
    List<Habit> findByUserIdAndStatusOrderByCreatedAtAsc(UUID userId, HabitStatus status);

    // Single habit scoped to a user — prevents accessing another user's habits by ID
    Optional<Habit> findByIdAndUserId(UUID id, UUID userId);

    // Count habits by status per user — used by admin module
    long countByUserIdAndStatus(UUID userId, HabitStatus status);
}
