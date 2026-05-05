-- ============================================================
-- V2 — Create users table
-- ============================================================

CREATE TABLE users (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_users          PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT ck_users_role     CHECK (role IN ('USER', 'ADMIN'))
);

-- Index for login lookup by username
CREATE INDEX idx_users_username ON users (username);

-- Index for lookup by email (used by admin queries)
CREATE INDEX idx_users_email ON users (email);

COMMENT ON TABLE  users              IS 'Registered application users';
COMMENT ON COLUMN users.role         IS 'USER = standard account, ADMIN = admin panel access';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password — plain text never stored';
