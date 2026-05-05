package com.tracker.checkin.repository;

import com.tracker.checkin.domain.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for the checkins table.
 *
 * All queries are scoped to user_id — same security boundary pattern as HabitRepository.
 */
@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    // All check-ins for a user on a specific date (powers the daily dashboard)
    List<CheckIn> findByUserIdAndDate(UUID userId, LocalDate date);

    // Single check-in for a specific habit on a specific date
    Optional<CheckIn> findByHabitIdAndUserIdAndDate(UUID habitId, UUID userId, LocalDate date);

    // Paginated history for a habit — most recent dates first
    @Query("""
            SELECT c FROM CheckIn c
            WHERE c.habit.id = :habitId
              AND c.user.id  = :userId
            ORDER BY c.date DESC
            """)
    Page<CheckIn> findHistoryByHabitAndUser(
            @Param("habitId") UUID habitId,
            @Param("userId")  UUID userId,
            Pageable pageable
    );

    // Total check-ins for a user — used by admin module
    long countByUserId(UUID userId);
}
