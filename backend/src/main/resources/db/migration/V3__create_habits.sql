-- ============================================================
-- V3 — Create habits table
-- ============================================================

CREATE TABLE habits (
    id          UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID          NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_habits        PRIMARY KEY (id),
    CONSTRAINT fk_habits_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_habits_status CHECK (status IN ('ACTIVE', 'ARCHIVED', 'DELETED'))
);

-- Fetch all active habits for a user (most common query — daily dashboard)
CREATE INDEX idx_habits_user_status ON habits (user_id, status);

COMMENT ON TABLE  habits            IS 'User-defined habits to track daily';
COMMENT ON COLUMN habits.status     IS 'ACTIVE = visible on dashboard, ARCHIVED = hidden but restorable, DELETED = permanently removed';
COMMENT ON COLUMN habits.updated_at IS 'Updated on every status change or name/description edit';
