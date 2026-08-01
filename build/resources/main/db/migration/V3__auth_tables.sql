-- USERS
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    email         TEXT NOT NULL UNIQUE,
    password_hash TEXT,
    status        TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','active','blocked')),
    confirmed_at  TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    role          TEXT NOT NULL DEFAULT 'user',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- USER PROFILES
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id    BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name TEXT,
    last_name  TEXT,
    avatar_url TEXT,
    locale     TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- OTP TOKENS
CREATE TABLE IF NOT EXISTS otp_tokens (
    email         TEXT NOT NULL,
    purpose       TEXT NOT NULL,                     -- REGISTER | LOGIN | RESET_PASSWORD
    code_hash     TEXT NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,
    attempts_left INTEGER NOT NULL CHECK (attempts_left BETWEEN 0 AND 10),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (email, purpose)
);
CREATE INDEX IF NOT EXISTS idx_otp_tokens_email_purpose_active ON otp_tokens(email, purpose, is_active);
CREATE INDEX IF NOT EXISTS idx_otp_tokens_expires_at ON otp_tokens(expires_at);

-- REFRESH TOKENS
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  TEXT NOT NULL UNIQUE,
    issued_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    user_agent  TEXT,
    ip          TEXT,
    device_id   TEXT
);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_active ON refresh_tokens(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- USER PURCHASES
CREATE TABLE IF NOT EXISTS user_purchases (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id          BIGINT NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    platform         VARCHAR(10) NOT NULL,          -- 'google' | 'apple'
    store_product_id TEXT NOT NULL,
    purchase_token   TEXT NOT NULL UNIQUE,
    order_id         TEXT UNIQUE,
    acknowledged     BOOLEAN NOT NULL DEFAULT FALSE,
    purchased_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at       TIMESTAMPTZ,
    UNIQUE (user_id, book_id)
);
