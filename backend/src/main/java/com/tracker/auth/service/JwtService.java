package com.tracker.auth.service;

import com.tracker.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Handles all JWT operations: generate, validate, extract claims.
 *
 * This service has no knowledge of HTTP or Spring Security filters —
 * it only understands tokens. The filter chain uses it as a utility.
 *
 * Algorithm : HS256 (HMAC-SHA256)
 * Claims    : subject (username), role, issued-at, expiry
 *
 * Secret requirement: minimum 64 characters for HS256 security.
 * Set via JWT_SECRET environment variable — never hardcode.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry-hours}")
    private long expiryHours;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT service initialised  expiry_hours={}", expiryHours);
    }

    // ── Token generation ─────────────────────────────────────────────────────

    public String generateToken(User user) {
        Instant now    = Instant.now();
        Instant expiry = now.plus(expiryHours, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();

        log.debug("Token generated  username={}  expires_at={}", user.getUsername(), expiry);
        return token;
    }

    public Instant getExpiry() {
        return Instant.now().plus(expiryHours, ChronoUnit.HOURS);
    }

    // ── Token validation ─────────────────────────────────────────────────────

    /**
     * Returns true if the token is valid and not expired.
     * Logs the reason on failure — useful for debugging auth issues.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException ex) {
            log.warn("JWT validation failed  reason={}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.warn("JWT validation error  reason={}", ex.getMessage());
            return false;
        }
    }

    // ── Claims extraction ─────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
