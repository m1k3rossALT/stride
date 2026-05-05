package com.tracker.checkin.domain;

import com.tracker.auth.domain.User;
import com.tracker.habit.domain.Habit;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single check-in record — represents whether a habit was completed on a specific date.
 *
 * Domain rules:
 *   - One record per habit per user per date (enforced by DB unique constraint)
 *   - is_checked defaults to false — the record is created when the dashboard loads,
 *     then toggled true when the user checks it off
 *   - date uses LocalDate (no time component) — timezone handling is the frontend's concern
 *   - Cascade deletes from both habit and user (see V4 migration FKs)
 */
@Entity
@Table(name = "checkins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false, updatable = false)
    private Habit habit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDate date;

    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private boolean checked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
