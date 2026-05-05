package com.tracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * DTOs for the auth module.
 *
 * Keeping all DTOs in one file per module reduces file noise for small modules.
 * Split into separate files if this grows beyond 4-5 records.
 *
 * Inbound (requests) — validated with @Valid on the controller
 * Outbound (responses) — plain records, no validation needed
 */
public class AuthDtos {

    // ── Inbound ──────────────────────────────────────────────────────────────

    public record RegisterRequest(

            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "A valid email address is required")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password

    ) {}

    public record LoginRequest(

            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            String password

    ) {}

    // ── Outbound ─────────────────────────────────────────────────────────────

    public record AuthResponse(
            String       token,
            String       username,
            String       role,
            Instant      expiresAt
    ) {}

    public record RegisterResponse(
            UUID    id,
            String  username,
            String  email,
            Instant createdAt
    ) {}
}
