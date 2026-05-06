package com.tracker.admin.controller;

import com.tracker.admin.dto.AdminDtos.*;
import com.tracker.admin.service.AdminService;
import com.tracker.auth.domain.User;
import com.tracker.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin endpoints — require a valid JWT AND ROLE_ADMIN.
 *
 * @PreAuthorize("hasRole('ADMIN')") on each method means:
 *   - A valid JWT is required (handled by JwtAuthFilter)
 *   - The JWT's role claim must be ADMIN (enforced here)
 *   - Any other role gets 403 FORBIDDEN via GlobalExceptionHandler
 *
 * GET /api/v1/admin/stats              — system-wide counts
 * GET /api/v1/admin/users              — all users, no credentials/PII
 * GET /api/v1/admin/users/{id}/activity — habit + check-in counts for one user
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── GET /api/v1/admin/stats ───────────────────────────────────────────────

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemStats>> getSystemStats(
            @AuthenticationPrincipal User admin
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getSystemStats(admin)));
    }

    // ── GET /api/v1/admin/users ───────────────────────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserSummary>>> getAllUsers(
            @AuthenticationPrincipal User admin
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers(admin)));
    }

    // ── GET /api/v1/admin/users/{id}/activity ─────────────────────────────────

    @GetMapping("/users/{id}/activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserActivity>> getUserActivity(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUserActivity(id, admin)));
    }
}
