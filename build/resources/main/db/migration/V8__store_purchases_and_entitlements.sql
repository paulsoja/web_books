-- Store IDs map to existing shared book IDs. No legacy grants are copied.
CREATE TABLE store_product_mappings (
    platform VARCHAR(10) NOT NULL CHECK (platform IN ('IOS', 'ANDROID')),
    store_product_id VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    PRIMARY KEY (platform, store_product_id)
);

CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    platform VARCHAR(10) NOT NULL CHECK (platform IN ('IOS', 'ANDROID')),
    product_id BIGINT NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    store_product_id VARCHAR(255) NOT NULL,
    store_transaction_id VARCHAR(255) NOT NULL,
    environment VARCHAR(10) NOT NULL CHECK (environment IN ('SANDBOX', 'PRODUCTION')),
    purchased_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE (platform, store_transaction_id)
);

CREATE TABLE entitlements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES books(id) ON DELETE RESTRICT,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);
