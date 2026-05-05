package com.tracker.habit.service;

import com.tracker.auth.domain.User;
import com.tracker.common.exception.AppException;
import com.tracker.common.exception.ErrorCode;
import com.tracker.habit.domain.Habit;
import com.tracker.habit.domain.HabitStatus;
import com.tracker.habit.dto.HabitDtos.*;
import com.tracker.habit.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * All business logic for habits.
 *
 * Ownership rule: every method receives the authenticated User object
 * from the controller. The repository is always queried with both the
 * habit ID and the user ID — a user can never touch another user's habits.
 *
 * Status transition rules (enforced here, documented in HabitStatus):
 *   ACTIVE   → ARCHIVED  via archive()
 *   ACTIVE   → DELETED   via delete()
 *   ARCHIVED → ACTIVE    via restore()
 *   ARCHIVED → DELETED   via delete()
 *   DELETED  → anything  not permitted
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public HabitResponse create(CreateHabitRequest request, User user) {
        log.info("Creating habit  user_id={}  name='{}'", user.getId(), request.name());

        Habit habit = Habit.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .status(HabitStatus.ACTIVE)
                .build();

        Habit saved = habitRepository.save(habit);
        log.info("Habit created  user_id={}  habit_id={}  name='{}'",
                user.getId(), saved.getId(), saved.getName());

        return toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HabitResponse> getActive(User user) {
        log.debug("Fetching active habits  user_id={}", user.getId());
        return habitRepository
                .findByUserIdAndStatusOrderByCreatedAtAsc(user.getId(), HabitStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> getArchived(User user) {
        log.debug("Fetching archived habits  user_id={}", user.getId());
        return habitRepository
                .findByUserIdAndStatusOrderByCreatedAtAsc(user.getId(), HabitStatus.ARCHIVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Transactional
    public HabitResponse update(UUID habitId, UpdateHabitRequest request, User user) {
        log.info("Updating habit  user_id={}  habit_id={}", user.getId(), habitId);

        Habit habit = findOwnedHabit(habitId, user);
        guardNotDeleted(habit);

        habit.setName(request.name());
        habit.setDescription(request.description());

        Habit saved = habitRepository.save(habit);
        log.info("Habit updated  user_id={}  habit_id={}  new_name='{}'",
                user.getId(), habitId, saved.getName());

        return toResponse(saved);
    }

    // ── Archive ───────────────────────────────────────────────────────────────

    @Transactional
    public HabitResponse archive(UUID habitId, User user) {
        log.info("Archiving habit  user_id={}  habit_id={}", user.getId(), habitId);

        Habit habit = findOwnedHabit(habitId, user);
        guardNotDeleted(habit);

        habit.setStatus(HabitStatus.ARCHIVED);
        Habit saved = habitRepository.save(habit);

        log.info("Habit archived  user_id={}  habit_id={}", user.getId(), habitId);
        return toResponse(saved);
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    @Transactional
    public HabitResponse restore(UUID habitId, User user) {
        log.info("Restoring habit  user_id={}  habit_id={}", user.getId(), habitId);

        Habit habit = findOwnedHabit(habitId, user);
        guardNotDeleted(habit);

        if (habit.getStatus() != HabitStatus.ARCHIVED) {
            log.warn("Restore rejected — habit is not archived  user_id={}  habit_id={}  status={}",
                    user.getId(), habitId, habit.getStatus());
            throw new AppException(ErrorCode.HABIT_NOT_ARCHIVED);
        }

        habit.setStatus(HabitStatus.ACTIVE);
        Habit saved = habitRepository.save(habit);

        log.info("Habit restored  user_id={}  habit_id={}", user.getId(), habitId);
        return toResponse(saved);
    }

    // ── Delete (permanent) ────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID habitId, User user) {
        log.info("Permanent delete requested  user_id={}  habit_id={}", user.getId(), habitId);

        Habit habit = findOwnedHabit(habitId, user);
        guardNotDeleted(habit);

        // Hard delete — removes habit and cascades to check-ins (handled at DB level)
        habitRepository.delete(habit);

        log.info("Habit permanently deleted  user_id={}  habit_id={}", user.getId(), habitId);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Fetches a habit that belongs to the given user.
     * Throws HABIT_NOT_FOUND if it doesn't exist or belongs to someone else.
     * This is the single ownership enforcement point for this service.
     */
    private Habit findOwnedHabit(UUID habitId, User user) {
        return habitRepository.findByIdAndUserId(habitId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Habit not found or not owned  user_id={}  habit_id={}",
                            user.getId(), habitId);
                    return new AppException(ErrorCode.HABIT_NOT_FOUND);
                });
    }

    private void guardNotDeleted(Habit habit) {
        if (habit.getStatus() == HabitStatus.DELETED) {
            log.warn("Operation rejected — habit is already deleted  habit_id={}", habit.getId());
            throw new AppException(ErrorCode.HABIT_ALREADY_DELETED);
        }
    }

    private HabitResponse toResponse(Habit habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getStatus(),
                habit.getCreatedAt(),
                habit.getUpdatedAt()
        );
    }
}
