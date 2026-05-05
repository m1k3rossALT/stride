package com.tracker.auth.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per request (after MdcRequestFilter).
 *
 * Flow:
 *   1. Read Authorization header — expects "Bearer <token>"
 *   2. If no token → pass through (public endpoints handled by security config)
 *   3. Validate the token via JwtService
 *   4. Load user from DB
 *   5. Populate Spring Security context — downstream code can call SecurityContextHolder
 *   6. If token is invalid → log warning, leave security context empty (request will 401)
 *
 * This filter never writes an HTTP response directly — that is the GlobalExceptionHandler's job.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX         = "Bearer ";

    private final JwtService            jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // No token — pass through; security config decides if the route is public
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token    = authHeader.substring(BEARER_PREFIX.length());
        String username = null;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception ex) {
            log.warn("JWT extraction failed  reason={}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Only authenticate if we have a username and no existing auth in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isValid(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Security context populated  username={}  role={}",
                        username, jwtService.extractRole(token));
            } else {
                log.warn("Invalid JWT on protected route  username={}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
