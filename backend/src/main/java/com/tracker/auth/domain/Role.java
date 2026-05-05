package com.tracker.auth.domain;

/**
 * Application roles.
 *
 * USER  — standard account, accesses their own habits and check-ins only
 * ADMIN — read-only access to admin panel (user list, activity counts)
 *
 * Stored as a string in the DB (see V2 migration CHECK constraint).
 * Spring Security authorities are prefixed: ROLE_USER, ROLE_ADMIN
 */
public enum Role {
    USER,
    ADMIN
}
