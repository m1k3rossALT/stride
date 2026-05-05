package com.tracker.auth.service;

import com.tracker.auth.domain.Role;
import com.tracker.auth.domain.User;
import com.tracker.auth.dto.AuthDtos.AuthResponse;
import com.tracker.auth.dto.AuthDtos.LoginRequest;
import com.tracker.auth.dto.AuthDtos.RegisterRequest;
import com.tracker.auth.dto.AuthDtos.RegisterResponse;
import com.tracker.auth.repository.UserRepository;
import com.tracker.common.exception.AppException;
import com.tracker.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for user registration and login.
 *
 * What this service does:
 *   register — validates uniqueness, hashes password, persists user, returns profile
 *   login    — verifies credentials, issues JWT
 *
 * What this service does NOT do:
 *   - Handle HTTP (that is the controller's job)
 *   - Know about filters or security context (that is the config's job)
 *   - Store plain-text passwords (ever)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService      jwtService;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registration attempt  username={}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed — username taken  username={}", request.username());
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                    "Username '" + request.username() + "' is already in use");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed — email taken  email={}", request.email());
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                    "An account with this email already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);

        log.info("Registration successful  user_id={}  username={}", saved.getId(), saved.getUsername());

        return new RegisterResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getCreatedAt()
        );
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt  username={}", request.username());

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    // Deliberately vague error — do not reveal whether username or password was wrong
                    log.warn("Login failed — user not found  username={}", request.username());
                    return new AppException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed — wrong password  username={}", request.username());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtService.generateToken(user);

        log.info("Login successful  user_id={}  username={}  role={}",
                user.getId(), user.getUsername(), user.getRole());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                jwtService.getExpiry()
        );
    }
}
