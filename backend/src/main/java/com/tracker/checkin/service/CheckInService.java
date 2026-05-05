package com.tracker.checkin.service;

import com.tracker.auth.domain.User;
import com.tracker.checkin.domain.CheckIn;
import com.tracker.checkin.dto.CheckInDtos.*;
import com.tracker.checkin.repository.CheckInRepository;
import com.tracker.common.exception.AppException;
import com.tracker.common.exception.ErrorCode;
import com.tracker.habit.domain.Habit;
import com.tracker.habit.domain.HabitStatus;
import com.tracker.habit.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for daily check-ins.
 *
 * Key design decisions:
 *
 * Today view — returns all ACTIVE habits merged with their check-in records for the
 * requested date. If a check-in record doesn't exist yet for a habit, isChecked
 * defaults to false. This means the frontend never needs to handle null check-in state.
 *
 * Bulk upsert — instead of insert-or-fail, we upsert: if a check-in exists for the
 * habit + date, update it; if not, create it. This makes the API idempotent — the
 * frontend can re-submit the full day's state safely.
 *
 * History — paginated, most recent dates first. Page size capped at 90 days to keep
 * response sizes predictable without requiring infinite scroll on the frontend.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private static final int MAX_HISTORY_PAGE_SIZE = 90;

    private final CheckInRepository checkInRepository;
    private final HabitRepository   habitRepository;

    // ── Today view ───────────────────────────────────────────────────────────

    /**
     * Returns all active habits for the user merged with their check-in state for the given date.
     * Missing check-in records are represented as isChecked = false (not as missing rows).
     */
    @Transactional(readOnly = true)
    public List<TodayHabitView> getTodayView(User user, LocalDate date) {
        log.debug("Building today view  user_id={}  date={}", user.getId(), date);

        // Fetch all active habits for the user
        List<Habit> activeHabits = habitRepository
                .findByUserIdAndStatusOrderByCreatedAtAsc(user.getId(), HabitStatus.ACTIVE);

        // Fetch all check-in records for this user on this date — single query
        Map<UUID, CheckIn> checkInsByHabitId = checkInRepository
                .findByUserIdAndDate(user.getId(), date)
                .stream()
                .collect(Collectors.toMap(c -> c.getHabit().getId(), c -> c));

        // Merge habits with their check-in state
        List<TodayHabitView> result = activeHabits.stream()
                .map(habit -> {
                    CheckIn checkIn = checkInsByHabitId.get(habit.getId());
                    return new TodayHabitView(
                            habit.getId(),
                            habit.getName(),
                            habit.getDescription(),
                            date,
                            checkIn != null && checkIn.isChecked()
                    );
                })
                .toList();

        log.debug("Today view built  user_id={}  date={}  habit_count={}  checked_count={}",
                user.getId(), date, result.size(),
                result.stream().filter(TodayHabitView::isChecked).count());

        return result;
    }

    // ── Bulk upsert ───────────────────────────────────────────────────────────

    /**
     * Upserts check-in state for all habits submitted.
     * Existing records are updated; missing records are created.
     * Habits not included in the request are not touched.
     */
    @Transactional
    public List<TodayHabitView> bulkUpsert(BulkCheckInRequest request, User user) {
        log.info("Bulk check-in  user_id={}  date={}  item_count={}",
                user.getId(), request.date(), request.items().size());

        for (BulkCheckInRequest.CheckInItem item : request.items()) {
            upsertSingle(item.habitId(), user, request.date(), item.checked());
        }

        log.info("Bulk check-in complete  user_id={}  date={}  checked_count={}",
                user.getId(), request.date(),
                request.items().stream().filter(BulkCheckInRequest.CheckInItem::checked).count());

        // Return the updated today view so the frontend can refresh state in one round trip
        return getTodayView(user, request.date());
    }

    // ── History ───────────────────────────────────────────────────────────────

    /**
     * Returns paginated check-in history for a single habit.
     * Verifies the habit belongs to the user before returning any data.
     */
    @Transactional(readOnly = true)
    public Page<CheckInHistoryEntry> getHistory(UUID habitId, User user, int page, int size) {
        // Cap page size — prevents accidentally requesting huge result sets
        int safeSize = Math.min(size, MAX_HISTORY_PAGE_SIZE);

        log.debug("Fetching check-in history  user_id={}  habit_id={}  page={}  size={}",
                user.getId(), habitId, page, safeSize);

        // Verify ownership before returning history
        habitRepository.findByIdAndUserId(habitId, user.getId())
                .orElseThrow(() -> {
                    log.warn("History request — habit not found or not owned  user_id={}  habit_id={}",
                            user.getId(), habitId);
                    return new AppException(ErrorCode.HABIT_NOT_FOUND);
                });

        return checkInRepository
                .findHistoryByHabitAndUser(habitId, user.getId(), PageRequest.of(page, safeSize))
                .map(c -> new CheckInHistoryEntry(c.getDate(), c.isChecked()));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void upsertSingle(UUID habitId, User user, LocalDate date, boolean checked) {
        // Verify the habit belongs to this user before touching its check-in
        Habit habit = habitRepository.findByIdAndUserId(habitId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Bulk check-in — habit not found or not owned  user_id={}  habit_id={}",
                            user.getId(), habitId);
                    return new AppException(ErrorCode.HABIT_NOT_FOUND);
                });

        checkInRepository
                .findByHabitIdAndUserIdAndDate(habitId, user.getId(), date)
                .ifPresentOrElse(
                        existing -> {
                            // Update existing record
                            existing.setChecked(checked);
                            checkInRepository.save(existing);
                            log.debug("Check-in updated  habit_id={}  date={}  checked={}",
                                    habitId, date, checked);
                        },
                        () -> {
                            // Create new record
                            CheckIn newCheckIn = CheckIn.builder()
                                    .habit(habit)
                                    .user(user)
                                    .date(date)
                                    .checked(checked)
                                    .build();
                            checkInRepository.save(newCheckIn);
                            log.debug("Check-in created  habit_id={}  date={}  checked={}",
                                    habitId, date, checked);
                        }
                );
    }
}
