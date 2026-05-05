package com.tracker.habit.domain;

import com.tracker.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Habit entity — a single trackable item belonging to a user.
 *
 * Domain rules enforced here:
 *   - A habit always belongs to exactly one user (non-nullable FK)
 *   - Status defaults to ACTIVE on creation
 *   - updatedAt is refreshed on every change via @PreUpdate
 *   - Name is trimmed before persistence via @PrePersist / @PreUpdate
 */
@Entity
@Table(name = "habits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private HabitStatus status = HabitStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // ── Lifecycle hooks ──────────────────────────────────────────────────────

    @PrePersist
    public void onPersist() {
        this.name      = name.trim();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.name      = name.trim();
        this.updatedAt = Instant.now();
    }
}
