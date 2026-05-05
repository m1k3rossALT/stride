package com.tracker.auth.controller;

import com.tracker.auth.dto.AuthDtos.AuthResponse;
import com.tracker.auth.dto.AuthDtos.LoginRequest;
import com.tracker.auth.dto.AuthDtos.RegisterRequest;
import com.tracker.auth.dto.AuthDtos.RegisterResponse;
import com.tracker.auth.service.AuthService;
import com.tracker.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth endpoints — all public (no JWT required).
 *
 * POST /api/v1/auth/register  — create a new user account
 * POST /api/v1/auth/login     — authenticate and receive a JWT
 *
 * Controller responsibilities:
 *   - Accept and validate the request (@Valid)
 *   - Delegate all logic to AuthService
 *   - Wrap the result in ApiResponse
 *   - Return the correct HTTP status
 *
 * Nothing else. No business logic lives here.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── POST /api/v1/auth/register ───────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    // ── POST /api/v1/auth/login ──────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
