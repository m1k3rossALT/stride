-- ============================================================
-- V1 — Schema baseline
-- ============================================================
-- This is the foundation migration.
-- All subsequent migrations (V2, V3, ...) build on this.
--
-- Convention for future migrations:
--   V2__create_users.sql
--   V3__create_habits.sql
--   V4__create_checkins.sql
--   V5__some_feature_change.sql
--
-- NEVER edit a migration file once it has been run.
-- ALWAYS create a new Vn__ file for changes.
-- ============================================================

-- Enable pgcrypto so we can generate UUIDs natively in the DB
-- gen_random_uuid() is used as the default for all primary keys
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
