package com.tracker.auth.service;

import com.tracker.auth.repository.UserRepository;
import com.tracker.common.exception.AppException;
import com.tracker.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bridges Spring Security's authentication mechanism with our UserRepository.
 *
 * Spring Security calls loadUserByUsername() during the JWT filter chain
 * to retrieve the full user object after validating a token.
 *
 * Kept separate from AuthService so each class has one responsibility.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user for security context  username={}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Security context load failed — user not found  username={}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }
}
