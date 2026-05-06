package com.tracker.admin.service;

import com.tracker.admin.dto.AdminDtos.*;
import com.tracker.auth.domain.User;
import com.tracker.auth.repository.UserRepository;
import com.tracker.checkin.repository.CheckInRepository;
import com.tracker.common.exception.AppException;
import com.tracker.common.exception.ErrorCode;
import com.tracker.habit.domain.HabitStatus;
import com.tracker.habit.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for the admin panel.
 *
 * Every method logs the admin's identity alongside the action.
 * This creates an audit trail of who accessed what and when —
 * visible in application logs without storing sensitive data.
 *
 * Data returned:
 *   - User list: id, username, role, created_at only
 *   - User activity: habit counts and check-in totals only
 *   - System stats: aggregate counts only
 *
 * Data never returned:
 *   - Passwords or password hashes
 *   - Email addresses
 *   - Habit names or descriptions
 *   - Check-in content or dates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository    userRepository;
    private final HabitRepository   habitRepository;
    private final CheckInRepository checkInRepository;

    // ── User list ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserSummary> getAllUsers(User admin) {
        log.info("Admin user list accessed  admin_id={}  admin_username={}",
                admin.getId(), admin.getUsername());

        return userRepository.findAll()
                .stream()
                .map(user -> new UserSummary(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    // ── User activity ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserActivity getUserActivity(UUID userId, User admin) {
        log.info("Admin user activity accessed  admin_id={}  admin_username={}  target_user_id={}",
                admin.getId(), admin.getUsername(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Admin activity lookup — user not found  admin_id={}  target_user_id={}",
                            admin.getId(), userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        long activeHabits   = habitRepository.countByUserIdAndStatus(userId, HabitStatus.ACTIVE);
        long archivedHabits = habitRepository.countByUserIdAndStatus(userId, HabitStatus.ARCHIVED);
        long totalCheckIns  = checkInRepository.countByUserId(userId);

        log.debug("Activity fetched  target_user_id={}  active_habits={}  archived_habits={}  total_checkins={}",
                userId, activeHabits, archivedHabits, totalCheckIns);

        return new UserActivity(
                user.getId(),
                user.getUsername(),
                activeHabits,
                archivedHabits,
                totalCheckIns
        );
    }

    // ── System stats ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SystemStats getSystemStats(User admin) {
        log.info("Admin system stats accessed  admin_id={}  admin_username={}",
                admin.getId(), admin.getUsername());

        long totalUsers     = userRepository.count();
        long totalCheckIns  = checkInRepository.count();

        log.debug("System stats  total_users={}  total_checkins={}", totalUsers, totalCheckIns);

        return new SystemStats(totalUsers, totalCheckIns);
    }
}
