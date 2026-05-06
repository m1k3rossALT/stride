-- ============================================================
-- V5 — Seed admin account
-- ============================================================
-- Creates the initial admin user if one does not already exist.
-- Password is a BCrypt hash of the value set in ADMIN_PASSWORD.
--
-- DEFAULT HASH BELOW = BCrypt("changeme_replace_in_production")
--
-- IMPORTANT: Before going live, either:
--   (a) Change ADMIN_PASSWORD in .env and re-generate the hash, OR
--   (b) Update the hash below with: htpasswd -bnBC 10 "" yourpassword | tr -d ':\n'
--
-- The DO $$ block makes this idempotent — safe to run multiple times.
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM users WHERE role = 'ADMIN'
    ) THEN
        INSERT INTO users (id, username, email, password_hash, role, created_at)
        VALUES (
            gen_random_uuid(),
            'admin',
            'admin@stride.local',
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWa',
            'ADMIN',
            now()
        );
    END IF;
END $$;

COMMENT ON TABLE users IS 'Registered application users — admin seeded via V5 migration';
