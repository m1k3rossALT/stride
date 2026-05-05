-- ============================================================
-- V4 — Create checkins table
-- ============================================================

CREATE TABLE checkins (
    id         UUID      NOT NULL DEFAULT gen_random_uuid(),
    habit_id   UUID      NOT NULL,
    user_id    UUID      NOT NULL,
    date       DATE      NOT NULL,
    is_checked BOOLEAN   NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT pk_checkins       PRIMARY KEY (id),
    CONSTRAINT fk_checkins_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkins_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,

    -- One record per habit per day per user — enforced at DB level
    -- Attempting a duplicate insert will throw a constraint violation
    CONSTRAINT uq_checkins_habit_date UNIQUE (habit_id, user_id, date)
);

-- Daily dashboard query: all check-ins for a user on a given date
CREATE INDEX idx_checkins_user_date ON checkins (user_id, date);

-- History query: all check-ins for a specific habit ordered by date
CREATE INDEX idx_checkins_habit_date ON checkins (habit_id, date DESC);

COMMENT ON TABLE  checkins            IS 'Daily check-in records — one row per habit per day';
COMMENT ON COLUMN checkins.is_checked IS 'true = habit completed for this date, false = not completed';
COMMENT ON COLUMN checkins.date       IS 'Calendar date of the check-in (DATE type, no time component)';
